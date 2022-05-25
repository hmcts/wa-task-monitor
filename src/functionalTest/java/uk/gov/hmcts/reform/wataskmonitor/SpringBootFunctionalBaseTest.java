package uk.gov.hmcts.reform.wataskmonitor;

import io.restassured.RestAssured;
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
import uk.gov.hmcts.reform.wataskmonitor.services.AuthorizationProvider;
import uk.gov.hmcts.reform.wataskmonitor.services.IdamService;
import uk.gov.hmcts.reform.wataskmonitor.services.RoleAssignmentServiceApi;
import uk.gov.hmcts.reform.wataskmonitor.utils.Common;

import java.io.IOException;

import static com.fasterxml.jackson.databind.PropertyNamingStrategy.LOWER_CAMEL_CASE;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;
import static net.serenitybdd.rest.SerenityRest.given;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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

}
