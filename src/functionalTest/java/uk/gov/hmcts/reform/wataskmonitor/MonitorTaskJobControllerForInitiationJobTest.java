package uk.gov.hmcts.reform.wataskmonitor;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaVariable;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.MonitorTaskJobRequest;
import uk.gov.hmcts.reform.wataskmonitor.entities.TestAuthenticationCredentials;
import uk.gov.hmcts.reform.wataskmonitor.entities.TestVariables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wataskmonitor.config.SecurityConfiguration.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.wataskmonitor.controllers.MonitorTaskJobControllerUtility.expectedResponse;

@Slf4j
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LawOfDemeter"})
public class MonitorTaskJobControllerForInitiationJobTest extends SpringBootFunctionalBaseTest {

    private List<String> caseIds;

    private TestAuthenticationCredentials caseworkerCredentials;

    @Before
    public void setUp() {
        caseworkerCredentials = authorizationProvider.getNewTribunalCaseworker("wa-ft-test-r2-");
        caseIds = new ArrayList<>();

        //to avoid initiation job run on non-local environment
        assumeTrue(isInitiationTriggerFlagEnabled());
    }

    @After
    public void cleanUp() {
        authorizationProvider.deleteAccount(caseworkerCredentials.getAccount().getUsername());
        common.cleanUpTask(caseworkerCredentials.getHeaders(), caseIds);
    }

    @Test
    public void task_initiation_job_should_initiate_task_and_taskState_should_be_unassigned() {

        TestVariables taskVariables = common.setupTaskAndRetrieveIds();

        assertNotNull(taskVariables);
        assertNotNull(taskVariables.getTaskId());
        assertNotNull(taskVariables.getCaseId());

        caseIds.add(taskVariables.getCaseId());

        given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, serviceToken)
            .body(TestUtility.asJsonString(new MonitorTaskJobRequest(new JobDetails(JobName.INITIATION))))
            .when()
            .post("/monitor/tasks/jobs")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body(is(expectedResponse.apply(JobName.INITIATION.name())));

        Map<String, CamundaVariable> camundaVariableMap =
            common.getTaskVariables(caseworkerCredentials.getHeaders(), taskVariables.getTaskId());

        assertEquals(taskVariables.getCaseId(), camundaVariableMap.get("caseId").getValue());
        assertEquals("unassigned", camundaVariableMap.get("taskState").getValue());

    }


    @Test
    public void task_initiation_job_should_not_initiate_delayed_task_and_taskState_should_be_unconfigured() {
        TestVariables taskVariables = common.setupDelayedTaskAndRetrieveIds();

        assertNotNull(taskVariables);
        assertNotNull(taskVariables.getCaseId());
        assertNotNull(taskVariables.getTaskId());

        caseIds.add(taskVariables.getCaseId());

        given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, serviceToken)
            .body(TestUtility.asJsonString(new MonitorTaskJobRequest(new JobDetails(JobName.INITIATION))))
            .when()
            .post("/monitor/tasks/jobs")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body(is(expectedResponse.apply(JobName.INITIATION.name())));

        Map<String, CamundaVariable> camundaVariables =
            common.getTaskVariablesFromCamunda(caseworkerCredentials.getHeaders(), taskVariables.getTaskId());

        String caseId = ((HashMap) (((HashMap) camundaVariables).get("caseId"))).get("value").toString();
        String actualTaskState = ((HashMap) (((HashMap) camundaVariables).get("taskState"))).get("value").toString();
        Object actualCftTaskState = ((HashMap) camundaVariables).get("cftTaskState");

        assertEquals(taskVariables.getCaseId(), caseId);
        assertEquals("unconfigured", actualTaskState);
        assertNull(actualCftTaskState);

    }

    @Test
    public void task_initiation_job_should_initiate_only_default_task_and_not_initiate_delayed_task() {
        TestVariables defaultTaskVariables = common.setupTaskAndRetrieveIds();
        common.setupCftOrganisationalRoleAssignment(caseworkerCredentials.getHeaders());

        initiateTask(caseworkerCredentials.getHeaders(), defaultTaskVariables,
            "followUpOverdueReasonsForAppeal", "task name", "task title"
        );

        common.setupOrganisationalRoleAssignmentWithCustomAttributes(
            caseworkerCredentials.getHeaders(),
            Map.of(
                "primaryLocation", "765324",
                "jurisdiction", "IA"
            )
        );

        assertNotNull(defaultTaskVariables);
        assertNotNull(defaultTaskVariables.getCaseId());
        assertNotNull(defaultTaskVariables.getTaskId());

        caseIds.add(defaultTaskVariables.getCaseId());

        TestVariables delayedTaskVariables = common.setupDelayedTaskAndRetrieveIds();

        assertNotNull(delayedTaskVariables);
        assertNotNull(delayedTaskVariables.getCaseId());
        assertNotNull(delayedTaskVariables.getTaskId());

        caseIds.add(delayedTaskVariables.getCaseId());

        given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, serviceToken)
            .body(TestUtility.asJsonString(new MonitorTaskJobRequest(new JobDetails(JobName.INITIATION))))
            .when()
            .post("/monitor/tasks/jobs")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body(is(expectedResponse.apply(JobName.INITIATION.name())));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Map<String, CamundaVariable> defaultTaskCamundaVariables =
            common.getTaskVariables(caseworkerCredentials.getHeaders(), defaultTaskVariables.getTaskId());

        Map<String, CamundaVariable> delayedTaskCamundaVariables =
            common.getTaskVariablesFromCamunda(caseworkerCredentials.getHeaders(), delayedTaskVariables.getTaskId());

        Map<String, CamundaVariable> delayedTaskCftResponse =
            common.getTaskFromTaskManagementApi(caseworkerCredentials.getHeaders(), delayedTaskVariables.getTaskId());

        Map<String, CamundaVariable> defaultTaskCftResponse =
            common.getTaskFromTaskManagementApi(caseworkerCredentials.getHeaders(), defaultTaskVariables.getTaskId());

        String actualDefaultTaskCftTaskState = ((HashMap) (((HashMap) defaultTaskCftResponse)
            .get("task"))).get("task_state").toString();
        String actualDelayedTaskCftTaskState = String.valueOf(delayedTaskCftResponse.get("status"));

        assertEquals("unassigned", actualDefaultTaskCftTaskState);
        assertEquals(String.valueOf(HttpStatus.NOT_FOUND.value()), actualDelayedTaskCftTaskState);

        String actualDefaultTaskTaskState = defaultTaskCamundaVariables.get("taskState").getValue().toString();
        String actualDelayedTaskTaskState = ((HashMap) (((HashMap) delayedTaskCamundaVariables)
            .get("taskState"))).get("value").toString();

        assertEquals("unassigned", actualDefaultTaskTaskState);
        assertEquals("unconfigured", actualDelayedTaskTaskState);

    }

}
