package uk.gov.hmcts.reform.wataskmonitor.adhoc;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.wataskmonitor.SpringBootFunctionalBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.TestUtility;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaVariable;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.MonitorTaskJobRequest;
import uk.gov.hmcts.reform.wataskmonitor.entities.TestAuthenticationCredentials;

import java.util.List;
import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wataskmonitor.config.SecurityConfiguration.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.wataskmonitor.controllers.MonitorTaskJobControllerUtility.expectedResponse;

@Slf4j
public class MonitorTaskJobControllerForAdHocPendingTerminationTest extends SpringBootFunctionalBaseTest {

    private TestAuthenticationCredentials caseworkerCredentials;

    @Before
    public void setUp() {
        caseworkerCredentials = authorizationProvider
            .getNewTribunalCaseworker("wa-ft-test-r2-");
    }

    @After
    public void cleanUp() {
        authorizationProvider.deleteAccount(caseworkerCredentials.getAccount().getUsername());
    }

    @Test
    public void task_initiation_job_should_initiate_task_and_taskState_should_be_unassigned() {
        List<HistoricCamundaTask> oldestTask = common.getTasksFromHistory(caseworkerCredentials.getHeaders());

        assertNotNull(oldestTask);
        assertEquals(1, oldestTask.size());

        log.info("Testing on task id {}", oldestTask.get(0).getId());
        Map<String, CamundaVariable> camundaVariableMap =
            common.getTaskVariables(caseworkerCredentials.getHeaders(), oldestTask.get(0).getId());

        assertEquals("pendingTermination", camundaVariableMap.get("cftTaskState").getValue());

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

        camundaVariableMap =
            common.getTaskVariables(caseworkerCredentials.getHeaders(), oldestTask.get(0).getId());

        assertFalse(camundaVariableMap.containsKey("cftTaskState"));
    }
}
