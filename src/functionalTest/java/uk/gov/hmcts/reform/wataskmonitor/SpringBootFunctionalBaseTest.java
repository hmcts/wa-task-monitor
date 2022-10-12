package uk.gov.hmcts.reform.wataskmonitor;

import io.restassured.RestAssured;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.config.DocumentManagementFiles;
import uk.gov.hmcts.reform.wataskmonitor.config.GivensBuilder;
import uk.gov.hmcts.reform.wataskmonitor.config.RestApiActions;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.InitiateTaskRequest;
import uk.gov.hmcts.reform.wataskmonitor.entities.TestVariables;
import uk.gov.hmcts.reform.wataskmonitor.services.AuthorizationProvider;
import uk.gov.hmcts.reform.wataskmonitor.services.IdamService;
import uk.gov.hmcts.reform.wataskmonitor.services.RoleAssignmentServiceApi;
import uk.gov.hmcts.reform.wataskmonitor.utils.Common;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.LOWER_CAMEL_CASE;
import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE;
import static net.serenitybdd.rest.SerenityRest.given;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTime.CAMUNDA_DATA_TIME_FORMATTER;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.CASE_ID;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.CREATED;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.DUE_DATE;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.TASK_NAME;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.TASK_TYPE;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.TITLE;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.InitiateTaskOperation.INITIATION;

@Slf4j
@SpringBootTest
@ActiveProfiles({"functional"})
@RunWith(SpringIntegrationSerenityRunner.class)
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LawOfDemeter"})
public class SpringBootFunctionalBaseTest {

    public String serviceToken;
    protected GivensBuilder given;
    protected Common common;
    protected RestApiActions camundaApiActions;
    protected RestApiActions taskManagementApiActions;
    @Autowired
    protected AuthorizationProvider authorizationProvider;
    @Autowired
    protected CoreCaseDataApi coreCaseDataApi;
    @Autowired
    protected DocumentManagementFiles documentManagementFiles;
    @Autowired
    protected IdamService idamService;
    @Autowired
    protected RoleAssignmentServiceApi roleAssignmentServiceApi;
    @Value("${targets.camunda}")
    private String camundaUrl;
    @Value("${targets.instance}")
    private String testUrl;
    @Value("${targets.task-management.url}")
    private String taskManagementUrl;
    @Value("${enable_initiation_trigger_flag}")
    private boolean enableInitiationTriggerFlag;
    @Autowired
    private CamundaClient camundaClient;
    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    private static final String TASK_INITIATION_ENDPOINT = "task/{task-id}/initiation";

    @Before
    public void setUpGivens() throws IOException {
        RestAssured.baseURI = testUrl;
        RestAssured.useRelaxedHTTPSValidation();

        serviceToken = authTokenGenerator.generate();

        camundaApiActions = new RestApiActions(camundaUrl, LOWER_CAMEL_CASE).setUp();
        taskManagementApiActions = new RestApiActions(taskManagementUrl, SNAKE_CASE).setUp();

        documentManagementFiles.prepare();

        given = new GivensBuilder(
                camundaApiActions,
                authorizationProvider,
                coreCaseDataApi,
                documentManagementFiles
        );

        common = new Common(
                given,
                camundaApiActions,
                authorizationProvider,
                idamService,
                roleAssignmentServiceApi,
                camundaClient,
                taskManagementApiActions
        );
    }

    @Test
    public void givenMonitorTaskJobRequestWithNoServiceAuthenticationHeaderShouldReturnStatus401Response() {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .post("/monitor/tasks/jobs")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    protected boolean isInitiationTriggerFlagEnabled() {
        log.info("enableInitiationTriggerFlag : '{}'", enableInitiationTriggerFlag);
        return enableInitiationTriggerFlag;
    }

    protected void initiateTask(Headers authenticationHeaders, TestVariables testVariables,
                                String taskType, String taskName, String taskTitle) {

        ZonedDateTime createdDate = ZonedDateTime.now();
        String formattedCreatedDate = CAMUNDA_DATA_TIME_FORMATTER.format(createdDate);
        ZonedDateTime dueDate = createdDate.plusDays(1);
        String formattedDueDate = CAMUNDA_DATA_TIME_FORMATTER.format(dueDate);

        Map<String, Object> taskAttributes = new HashMap<>();
        taskAttributes.put(TASK_TYPE.value(), taskType);
        taskAttributes.put(TASK_NAME.value(), taskName);
        taskAttributes.put(TITLE.value(), taskTitle);
        taskAttributes.put(CASE_ID.value(), testVariables.getCaseId());
        taskAttributes.put(CREATED.value(), formattedCreatedDate);
        taskAttributes.put(DUE_DATE.value(), formattedDueDate);

        InitiateTaskRequest initiateTaskRequest = new InitiateTaskRequest(INITIATION, taskAttributes);

        Response result = taskManagementApiActions.post(
                TASK_INITIATION_ENDPOINT,
                testVariables.getTaskId(),
                initiateTaskRequest,
                authenticationHeaders
        );

        result.then().assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .and()
                .contentType(APPLICATION_JSON_VALUE)
                .body("task_id", equalTo(testVariables.getTaskId()))
                .body("case_id", equalTo(testVariables.getCaseId()));
    }

}
