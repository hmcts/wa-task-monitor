package uk.gov.hmcts.reform.wataskmonitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.MonitorTaskJobRequest;
import uk.gov.hmcts.reform.wataskmonitor.entities.TestAuthenticationCredentials;
import uk.gov.hmcts.reform.wataskmonitor.entities.TestVariables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wataskmonitor.config.SecurityConfiguration.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.wataskmonitor.controllers.MonitorTaskJobControllerUtility.expectedResponse;

@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LawOfDemeter"})
@Ignore("Enable when wa-initiate-tasks-on-create is enabled")
public class MonitorTaskJobControllerForTaskInitiationFailuresJobTest extends SpringBootFunctionalBaseTest {

    private List<String> caseIds;
    private TestAuthenticationCredentials caseworkerCredentials;

    @Before
    public void setUp() {
        caseworkerCredentials = authorizationProvider.getNewTribunalCaseworker("wa-ft-test-r2-");
        caseIds = new ArrayList<>();
    }

    @After
    public void cleanUp() {
        common.clearAllRoleAssignments(caseworkerCredentials.getHeaders());
        authorizationProvider.deleteAccount(caseworkerCredentials.getAccount().getUsername());
        common.cleanUpTask(caseworkerCredentials.getHeaders(), caseIds);
    }

    @Test
    public void should_initiate_unconfigured_task_when_task_initiation_failures_job_runs() {
        TestVariables taskVariables = common.setupTaskAndRetrieveIds();
        common.setupOrganisationalRoleAssignment(caseworkerCredentials.getHeaders());

        assertNotNull(taskVariables);
        assertNotNull(taskVariables.getTaskId());
        assertNotNull(taskVariables.getCaseId());

        caseIds.add(taskVariables.getCaseId());

        given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, serviceToken)
            .body(TestUtility.asJsonString(
                new MonitorTaskJobRequest(new JobDetails(JobName.TASK_INITIATION_FAILURES))
            ))
            .when()
            .post("/monitor/tasks/jobs")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body(is(expectedResponse.apply(JobName.TASK_INITIATION_FAILURES.name())));

        Map<String, ?> taskManagementResponse =
            common.getTaskFromTaskManagementApi(caseworkerCredentials.getHeaders(), taskVariables.getTaskId());

        Object taskObject = taskManagementResponse.get("task");
        assertTrue(taskObject instanceof Map<?, ?>);
        Map<?, ?> task = (Map<?, ?>) taskObject;

        assertEquals(taskVariables.getTaskId(), task.get("id"));
        assertEquals(taskVariables.getCaseId(), task.get("case_id"));
        assertEquals("unassigned", task.get("task_state"));
    }
}
