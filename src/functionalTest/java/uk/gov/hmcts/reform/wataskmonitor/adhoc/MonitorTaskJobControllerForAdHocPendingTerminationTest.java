package uk.gov.hmcts.reform.wataskmonitor.adhoc;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.wataskmonitor.SpringBootFunctionalBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.TestUtility;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoryVariableInstance;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.MonitorTaskJobRequest;
import uk.gov.hmcts.reform.wataskmonitor.entities.TestAuthenticationCredentials;
import uk.gov.hmcts.reform.wataskmonitor.entities.TestVariables;

import java.util.ArrayList;
import java.util.List;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wataskmonitor.config.SecurityConfiguration.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.wataskmonitor.controllers.MonitorTaskJobControllerUtility.expectedResponse;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.CFT_TASK_STATE;

@Slf4j
public class MonitorTaskJobControllerForAdHocPendingTerminationTest extends SpringBootFunctionalBaseTest {

    private TestAuthenticationCredentials caseworkerCredentials;

    private List<String> caseIds;

    @Before
    public void setUp() {
        caseworkerCredentials = authorizationProvider
            .getNewTribunalCaseworker("wa-ft-test-r2-");
        caseIds = new ArrayList<>();
    }

    @After
    public void cleanUp() {
        authorizationProvider.deleteAccount(caseworkerCredentials.getAccount().getUsername());
        common.cleanUpTask(caseworkerCredentials.getHeaders(), caseIds);
    }

    @Test
    public void task_delete_pending_termination_task_job_should_remove_cft_state_from_historic_tasks() {
        TestVariables taskVariables = common.setupTaskAndRetrieveIds();
        caseIds.add(taskVariables.getCaseId());
        common.updateTaskVariable(caseworkerCredentials.getHeaders(), taskVariables.getTaskId());
        List<HistoricCamundaTask> oldestTask = common.getTasksFromHistory(caseworkerCredentials.getHeaders());

        assertNotNull(oldestTask);
        assertEquals(1, oldestTask.size());

        String taskId = oldestTask.get(0).getId();

        log.info("Testing on task id {}", taskId);
        List<HistoryVariableInstance> variables =
            common.getTaskHistoryVariable(caseworkerCredentials.getHeaders(),
                taskId,
                CFT_TASK_STATE.value());

        assertEquals("pendingTermination", variables.get(0).getValue());

        given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, serviceToken)
            .body(TestUtility.asJsonString(new MonitorTaskJobRequest(
                new JobDetails(JobName.AD_HOC_PENDING_TERMINATION_TASKS))))
            .when()
            .post("/monitor/tasks/jobs")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body(is(expectedResponse.apply(JobName.AD_HOC_PENDING_TERMINATION_TASKS.name())));

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        variables =
            common.getTaskHistoryVariable(caseworkerCredentials.getHeaders(),
                taskId,
                CFT_TASK_STATE.value());

        assertTrue(variables.isEmpty());
    }

}
