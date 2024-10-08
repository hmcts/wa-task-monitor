package uk.gov.hmcts.reform.wataskmonitor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.idam.UserInfo;
import uk.gov.hmcts.reform.wataskmonitor.entities.TestAuthenticationCredentials;
import uk.gov.hmcts.reform.wataskmonitor.entities.camunda.CamundaProcessVariables;
import uk.gov.hmcts.reform.wataskmonitor.entities.camunda.CamundaSendMessageRequest;
import uk.gov.hmcts.reform.wataskmonitor.entities.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskmonitor.services.AuthorizationProvider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static java.time.ZonedDateTime.now;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wataskmonitor.config.SecurityConfiguration.AUTHORIZATION;
import static uk.gov.hmcts.reform.wataskmonitor.config.SecurityConfiguration.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.wataskmonitor.entities.camunda.CamundaMessage.CREATE_TASK_MESSAGE;
import static uk.gov.hmcts.reform.wataskmonitor.entities.camunda.CamundaProcessVariables.ProcessVariablesBuilder.processVariables;
import static uk.gov.hmcts.reform.wataskmonitor.utils.Common.CAMUNDA_DATA_TIME_FORMATTER;

@Slf4j
public class GivensBuilder {

    private final RestApiActions camundaApiActions;
    private final AuthorizationProvider authorizationProvider;

    private final CoreCaseDataApi coreCaseDataApi;

    public GivensBuilder(RestApiActions camundaApiActions,
                         AuthorizationProvider authorizationProvider,
                         CoreCaseDataApi coreCaseDataApi
    ) {
        this.camundaApiActions = camundaApiActions;
        this.authorizationProvider = authorizationProvider;
        this.coreCaseDataApi = coreCaseDataApi;

    }

    public String createCcdCase() {
        TestAuthenticationCredentials lawFirmCredentials = authorizationProvider.getNewLawFirm();
        String userToken = lawFirmCredentials.getHeaders().getValue(AUTHORIZATION);
        String serviceToken = lawFirmCredentials.getHeaders().getValue(SERVICE_AUTHORIZATION);
        UserInfo userInfo = authorizationProvider.getUserInfo(userToken);

        StartEventResponse startCase = coreCaseDataApi.startForCaseworker(
            userToken,
            serviceToken,
            userInfo.getUid(),
            "WA",
            "WaCaseType",
            "CREATE"
        );

        String resourceFilename = "requests/ccd/wa_case_data.json";

        Map data = null;
        try {
            String caseDataString =
                FileUtils.readFileToString(ResourceUtils.getFile("classpath:" + resourceFilename),
                    StandardCharsets.UTF_8);
            caseDataString = caseDataString.replace(
                "{NEXT_HEARING_DATE}",
                OffsetDateTime.now().toString()
            );

            data = new ObjectMapper().readValue(caseDataString, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startCase.getToken())
            .event(Event.builder()
                .id(startCase.getEventId())
                .summary("summary")
                .description("description")
                .build())
            .data(data)
            .build();
        log.info("caseDataContent [" + caseDataContent + "]");
        //Fire submit event
        CaseDetails caseDetails = coreCaseDataApi.submitForCaseworker(
            userToken,
            serviceToken,
            userInfo.getUid(),
            "WA",
            "WaCaseType",
            true,
            caseDataContent
        );

        log.info("Created case [" + caseDetails.getId() + "]");

        StartEventResponse submitCase = coreCaseDataApi.startEventForCaseWorker(
            userToken,
            serviceToken,
            userInfo.getUid(),
            "WA",
            "WaCaseType",
            caseDetails.getId().toString(),
            "START_PROGRESS"
        );

        CaseDataContent submitCaseDataContent = CaseDataContent.builder()
            .eventToken(submitCase.getToken())
            .event(Event.builder()
                .id(submitCase.getEventId())
                .summary("summary")
                .description("description")
                .build())
            .data(data)
            .build();

        coreCaseDataApi.submitEventForCaseWorker(
            userToken,
            serviceToken,
            userInfo.getUid(),
            "WA",
            "WaCaseType",
            caseDetails.getId().toString(),
            true,
            submitCaseDataContent
        );
        log.info("Submitted case [" + caseDetails.getId() + "]");

        return caseDetails.getId().toString();
    }

    public GivensBuilder createTaskWithCaseId(String caseId) {
        Map<String, CamundaValue<?>> processVariables = initiateProcessVariables(caseId);

        CamundaSendMessageRequest request = new CamundaSendMessageRequest(
            CREATE_TASK_MESSAGE.toString(),
            processVariables
        );

        Response result = camundaApiActions.post(
            "message",
            request,
            authorizationProvider.getServiceAuthorizationHeader()
        );

        result.then().assertThat()
            .statusCode(HttpStatus.NO_CONTENT.value());

        return this;
    }

    public GivensBuilder createDelayedTaskWithCaseId(String caseId) {
        Map<String, CamundaValue<?>> processVariables = initiateProcessVariablesForDelayedTask(caseId);

        CamundaSendMessageRequest request = new CamundaSendMessageRequest(
            CREATE_TASK_MESSAGE.toString(),
            processVariables
        );

        Response result = camundaApiActions.post(
            "message",
            request,
            authorizationProvider.getServiceAuthorizationHeader()
        );

        result.then().assertThat()
            .statusCode(HttpStatus.NO_CONTENT.value());

        return this;
    }


    public List<CamundaTask> retrieveTaskWithProcessVariableFilter(String key, String value) {
        log.info("Attempting to retrieve task with {} = {}", key, value);
        String filter = "?processVariables=" + key + "_eq_" + value;

        AtomicReference<List<CamundaTask>> response = new AtomicReference<>();
        await().ignoreException(AssertionError.class)
            .pollInterval(500, MILLISECONDS)
            .atMost(60, SECONDS)
            .until(
                () -> {
                    Response result = camundaApiActions.get(
                        "/task" + filter,
                        authorizationProvider.getServiceAuthorizationHeader()
                    );

                    result.then().assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .contentType(APPLICATION_JSON_VALUE)
                        .body("size()", is(1));

                    response.set(
                        result.then()
                            .extract()
                            .jsonPath().getList("", CamundaTask.class)
                    );

                    return true;
                });

        return response.get();
    }

    public List<CamundaTask> retrieveDelayedTaskWithProcessVariableFilter(String key, String value) {
        log.info("Attempting to retrieve task with {} = {}", key, value);
        String filter = "?variables=" + key + "_eq_" + value;

        AtomicReference<List<CamundaTask>> response = new AtomicReference<>();
        await().ignoreException(AssertionError.class)
            .pollInterval(500, MILLISECONDS)
            .atMost(60, SECONDS)
            .until(
                () -> {

                    Response result = camundaApiActions.get(
                        "process-instance" + filter,
                        authorizationProvider.getServiceAuthorizationHeader()
                    );

                    result.then().assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .contentType(APPLICATION_JSON_VALUE)
                        .body("size()", is(1));

                    response.set(
                        result.then()
                            .extract()
                            .jsonPath().getList("", CamundaTask.class)
                    );

                    return true;
                });

        return response.get();
    }

    public GivensBuilder and() {
        return this;
    }

    public Map<String, CamundaValue<?>> createDefaultTaskVariables(String caseId) {
        CamundaProcessVariables processVariables = processVariables()
            .withProcessVariable("caseId", caseId)
            .withProcessVariable("jurisdiction", "WA")
            .withProcessVariable("caseTypeId", "WaCaseType")
            .withProcessVariable("region", "1")
            .withProcessVariable("location", "765324")
            .withProcessVariable("locationName", "Taylor House")
            .withProcessVariable("staffLocation", "Taylor House")
            .withProcessVariable("securityClassification", "PUBLIC")
            .withProcessVariable("name", "task name")
            .withProcessVariable("taskId", "processApplication")
            .withProcessVariable("taskAttributes", "")
            .withProcessVariable("taskType", "processApplication")
            .withProcessVariable("taskCategory", "")
            .withProcessVariable("taskState", "unconfigured")
            //for testing-purposes
            .withProcessVariable("dueDate", now().plusDays(10).format(CAMUNDA_DATA_TIME_FORMATTER))
            .withProcessVariable("task-supervisor", "Read,Refer,Manage,Cancel")
            .withProcessVariable("tribunal-caseworker", "Read,Refer,Own,Manage,Cancel")
            .withProcessVariable("senior-tribunal-caseworker", "Read,Refer,Own,Manage,Cancel")
            .withProcessVariable("delayUntil", now().format(CAMUNDA_DATA_TIME_FORMATTER))
            .withProcessVariable("workingDaysAllowed", "2")
            .withProcessVariableBoolean("hasWarnings", false)
            //.withProcessVariable("warningList", (new WarningValues()).toString())
            .withProcessVariable("caseManagementCategory", "Protection")
            .withProcessVariable("description", "aDescription")
            .withProcessVariable("roleAssignmentId", "aRoleAssignmentId")
            .build();

        return processVariables.getProcessVariablesMap();
    }

    public Map<String, CamundaValue<?>> createDelayedTaskVariables(String caseId) {
        CamundaProcessVariables processVariables = processVariables()
            .withProcessVariable("caseId", caseId)
            .withProcessVariable("jurisdiction", "WA")
            .withProcessVariable("caseTypeId", "WaCaseType")
            .withProcessVariable("region", "1")
            .withProcessVariable("location", "765324")
            .withProcessVariable("locationName", "Taylor House")
            .withProcessVariable("staffLocation", "Taylor House")
            .withProcessVariable("securityClassification", "PUBLIC")
            .withProcessVariable("group", "TCW")
            .withProcessVariable("name", "task name")
            .withProcessVariable("taskId", "processApplication")
            .withProcessVariable("taskAttributes", "")
            .withProcessVariable("taskType", "processApplication")
            .withProcessVariable("taskCategory", "")
            .withProcessVariable("taskState", "unconfigured")
            //for testing-purposes
            .withProcessVariable("dueDate", now().plusDays(10).format(CAMUNDA_DATA_TIME_FORMATTER))
            .withProcessVariable("task-supervisor", "Read,Refer,Manage,Cancel")
            .withProcessVariable("tribunal-caseworker", "Read,Refer,Own,Manage,Cancel")
            .withProcessVariable("senior-tribunal-caseworker", "Read,Refer,Own,Manage,Cancel")
            .withProcessVariable("delayUntil", now().plusDays(2).format(CAMUNDA_DATA_TIME_FORMATTER))
            .withProcessVariable("workingDaysAllowed", "2")
            .withProcessVariableBoolean("hasWarnings", false)
            //.withProcessVariable("warningList", (new WarningValues()).toString())
            .withProcessVariable("caseManagementCategory", "Protection")
            .withProcessVariable("description", "aDescription")
            .build();

        return processVariables.getProcessVariablesMap();
    }

    private Map<String, CamundaValue<?>> initiateProcessVariables(String caseId) {
        return createDefaultTaskVariables(caseId);
    }

    private Map<String, CamundaValue<?>> initiateProcessVariablesForDelayedTask(String caseId) {
        return createDelayedTaskVariables(caseId);
    }

}
