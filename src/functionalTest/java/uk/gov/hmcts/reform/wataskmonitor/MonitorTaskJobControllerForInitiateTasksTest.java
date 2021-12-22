package uk.gov.hmcts.reform.wataskmonitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.clients.CaseEventHandlerClient;
import uk.gov.hmcts.reform.wataskmonitor.clients.model.CamundaProcessVariables;
import uk.gov.hmcts.reform.wataskmonitor.clients.model.ProcessVariable;
import uk.gov.hmcts.reform.wataskmonitor.domain.caseeventhandler.EventInformation;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.MonitorTaskJobRequest;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wataskmonitor.controllers.MonitorTaskJobControllerUtility.expectedResponse;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.INITIATION;

@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LawOfDemeter"})
public class MonitorTaskJobControllerForInitiateTasksTest extends SpringBootFunctionalBaseTest {

    private static String DELETE_REQUEST = "{\n"
                                           + "    \"deleteReason\": \"clean up running process instances\",\n"
                                           + "    \"processInstanceIds\": [\n"
                                           + "    \"{PROCESS_ID}\"\n"
                                           + "    ],\n"
                                           + "    \"skipCustomListeners\": true,\n"
                                           + "    \"skipSubprocesses\": true,\n"
                                           + "    \"failIfNotExists\": false\n"
                                           + "    }";

    @Autowired
    private CaseEventHandlerClient caseEventHandlerClient;

    @Autowired
    private CamundaClient camundaClient;

    List<String> caseIds = new ArrayList<>();

    @Before
    public void setup() {
        //create a task
        String caseId = generateCaseId();
        EventInformation eventInformation =
            createEventInformation(caseId, "buildCase", "caseUnderReview");
        sendMessageToInitiateTask(eventInformation);
        caseIds.add(caseId);

        //create a delayed task
        //caseId = generateCaseId();
        //eventInformation =
        //    createEventInformation(caseId, "removeRepresentation", null);
        //sendMessageToInitiateTask(eventInformation);
        //caseIds.add(caseId);

    }

    @After
    public void tearDown() {
        Set<CamundaProcessVariables> camundaProcessVariables = new HashSet<>();
        caseIds.forEach(caseId -> camundaProcessVariables.addAll(getProcesses(caseId)));


        camundaProcessVariables.forEach(processInstance -> getProcessesVariables(processInstance.getId()));


        //camundaProcessVariables.forEach(processInstance -> deleteProcessInstance(processInstance.getId()));
    }

    @Test
    public void given_monitor_task_job_request_should_return_status_200_and_expected_response() {
        given()
            .contentType(APPLICATION_JSON_VALUE)
            .header("ServiceAuthorization", serviceToken)
            .body(TestUtility.asJsonString(new MonitorTaskJobRequest(new JobDetails(INITIATION))))
            .when()
            .post("/monitor/tasks/jobs")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body(is(expectedResponse.apply(INITIATION.name())));
    }

    private void sendMessageToInitiateTask(EventInformation eventInformation) {
        caseEventHandlerClient.sendMessage(
            serviceToken,
            eventInformation
        );
    }

    private EventInformation createEventInformation(String caseId, String eventId, String newStateId) {
        return EventInformation.builder()
            .eventInstanceId(UUID.randomUUID().toString())
            .eventTimeStamp(LocalDateTime.now())
            .caseId(caseId)
            .jurisdictionId("ia")
            .caseTypeId("asylum")
            .eventId(eventId)
            .newStateId(newStateId)
            .userId("some user Id")
            .build();
    }

    private String generateCaseId() {
        int length = 16;
        SecureRandom secureRandom = new SecureRandom();
        String characters = "0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(secureRandom.nextInt(characters.length())));
        }
        return sb.toString();
    }

    private Set<CamundaProcessVariables> getProcesses(String caseId) {
        List<CamundaProcessVariables> camundaProcessVariables = camundaClient.getProcessInstancesByVariables(
            serviceToken,
            "caseId_eq_" + caseId,
            List.of("processStartTimer")
        );

        return Set.copyOf(camundaProcessVariables);
    }

    private Map<String, ProcessVariable> getProcessesVariables(String processId) {
        Map<String, ProcessVariable> processVariableMap = camundaClient.getProcessInstanceVariablesById(
            serviceToken,
            processId
        );

        System.out.println(processVariableMap.toString());

        return processVariableMap;
    }

    private void deleteProcessInstance(String processId) {
        String deleteRequest = DELETE_REQUEST.replace("{PROCESS_ID}", processId);
        camundaClient.deleteProcessInstance(serviceToken, deleteRequest);
    }

}
