package uk.gov.hmcts.reform.wataskmonitor.services.jobs.failure.initiation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskManagementClient;
import uk.gov.hmcts.reform.wataskmonitor.config.job.InitiationJobConfig;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaVariable;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation.InitiationTaskAttributesMapper;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation.helpers.InitiationHelpers;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.TASK_INITIATION_FAILURES;

class TaskInitiationFailuresJobServiceTest extends UnitBaseTest {

    @Mock
    private CamundaClient camundaClient;
    @Mock
    private TaskManagementClient taskManagementClient;
    @Mock
    private InitiationJobConfig initiationJobConfig;

    private InitiationTaskAttributesMapper initiationTaskAttributesMapper;
    private TaskInitiationFailuresJobService taskInitiationFailuresJobService;

    @Captor
    private ArgumentCaptor<String> actualQueryParametersCaptor;

    @BeforeEach
    void setUp() {
        initiationTaskAttributesMapper = new InitiationTaskAttributesMapper(new ObjectMapper());
        taskInitiationFailuresJobService = new TaskInitiationFailuresJobService(
            camundaClient,
            taskManagementClient,
            initiationTaskAttributesMapper,
            initiationJobConfig
        );
        lenient().when(initiationJobConfig.getCamundaMaxResults()).thenReturn("100");
        lenient().when(initiationJobConfig.isCamundaTimeLimitFlag()).thenReturn(true);
        lenient().when(initiationJobConfig.getCamundaTimeLimit()).thenReturn(120L);
    }

    @Test
    void should_return_active_tasks_and_not_delayed_tasks() throws JSONException {
        List<CamundaTask> tasks = createRetryableTasks();
        when(camundaClient.getTasks(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(tasks);
        stubVariablesFor(tasks);

        List<CamundaTask> actualCamundaTasks = getTasksFromReport(
            taskInitiationFailuresJobService.initiateFailedTasks(SOME_SERVICE_TOKEN)
        );

        assertQueryTargetsUserTasksAndNotDelayedTasks();
        assertQuery(true);
        assertThat(actualCamundaTasks).hasSameSizeAs(tasks);
    }

    @Test
    void when_no_tasks_should_generate_report() {
        when(camundaClient.getTasks(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(emptyList());

        GenericJobReport actual = taskInitiationFailuresJobService.initiateFailedTasks(SOME_SERVICE_TOKEN);

        GenericJobReport expectation = new GenericJobReport(0, emptyList());
        assertEquals(expectation, actual);
    }

    @Test
    void should_succeed_and_initiate_failed_tasks() {
        CamundaTask camundaTask = InitiationHelpers.createMockedCamundaTask(
            ZonedDateTime.now(),
            ZonedDateTime.now().plusDays(1)
        );
        List<CamundaTask> tasks = singletonList(camundaTask);
        Map<String, CamundaVariable> mockedVariables = InitiationHelpers.createMockCamundaVariables();

        when(camundaClient.getTasks(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(tasks);
        when(camundaClient.getVariables(SOME_SERVICE_TOKEN, camundaTask.getId()))
            .thenReturn(mockedVariables);

        GenericJobReport actual = taskInitiationFailuresJobService.initiateFailedTasks(SOME_SERVICE_TOKEN);

        verify(taskManagementClient, times(1))
            .initiateTask(anyString(), anyString(), any());

        GenericJobOutcome outcome = GenericJobOutcome.builder()
            .taskId(camundaTask.getId())
            .processInstanceId(camundaTask.getProcessInstanceId())
            .successful(true)
            .jobType(TASK_INITIATION_FAILURES.name())
            .build();

        GenericJobReport expectation = new GenericJobReport(1, singletonList(outcome));
        assertEquals(expectation, actual);
    }

    @Test
    void should_return_job_outcome_unsuccessful_when_initiation_fails() {
        CamundaTask camundaTask = InitiationHelpers.createMockedCamundaTask(
            ZonedDateTime.now(),
            ZonedDateTime.now().plusDays(1)
        );
        List<CamundaTask> tasks = singletonList(camundaTask);

        when(camundaClient.getTasks(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(tasks);
        when(camundaClient.getVariables(SOME_SERVICE_TOKEN, camundaTask.getId()))
            .thenReturn(InitiationHelpers.createMockCamundaVariables());
        doThrow(new RuntimeException("Task Management unavailable"))
            .when(taskManagementClient)
            .initiateTask(anyString(), anyString(), any());

        GenericJobReport actual = taskInitiationFailuresJobService.initiateFailedTasks(SOME_SERVICE_TOKEN);

        GenericJobOutcome outcome = GenericJobOutcome.builder()
            .taskId(camundaTask.getId())
            .processInstanceId(camundaTask.getProcessInstanceId())
            .successful(false)
            .jobType(TASK_INITIATION_FAILURES.name())
            .build();

        GenericJobReport expectation = new GenericJobReport(1, singletonList(outcome));
        assertEquals(expectation, actual);
    }

    @Test
    void should_only_report_initiation_failures_for_legacy_flow() {
        CamundaTask camundaTask = InitiationHelpers.createMockedCamundaTask(
            ZonedDateTime.now(),
            ZonedDateTime.now().plusDays(1)
        );

        when(camundaClient.getTasks(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(singletonList(camundaTask));
        when(camundaClient.getVariables(SOME_SERVICE_TOKEN, camundaTask.getId()))
            .thenReturn(InitiationHelpers.createMockCamundaVariables());

        GenericJobReport actual = taskInitiationFailuresJobService.getInitiationFailures(SOME_SERVICE_TOKEN);

        GenericJobOutcome outcome = GenericJobOutcome.builder()
            .taskId(camundaTask.getId())
            .processInstanceId(camundaTask.getProcessInstanceId())
            .successful(true)
            .jobType(TASK_INITIATION_FAILURES.name())
            .build();
        assertEquals(new GenericJobReport(1, singletonList(outcome)), actual);
        verifyNoInteractions(taskManagementClient);
    }

    @Test
    void should_createdAfter_exists_or_not_in_query_according_to_initiation_flag() throws JSONException {
        CamundaTask camundaTask = InitiationHelpers.createMockedCamundaTask(
            ZonedDateTime.now(),
            ZonedDateTime.now().plusDays(1)
        );

        when(initiationJobConfig.isCamundaTimeLimitFlag()).thenReturn(false);
        when(initiationJobConfig.getCamundaMaxResults()).thenReturn("10");
        when(camundaClient.getTasks(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("10"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(singletonList(camundaTask));
        when(camundaClient.getVariables(SOME_SERVICE_TOKEN, camundaTask.getId()))
            .thenReturn(InitiationHelpers.createMockCamundaVariables());

        taskInitiationFailuresJobService.initiateFailedTasks(SOME_SERVICE_TOKEN);

        assertQuery(false);
    }

    private void stubVariablesFor(List<CamundaTask> tasks) {
        tasks.forEach(task -> when(camundaClient.getVariables(SOME_SERVICE_TOKEN, task.getId()))
            .thenReturn(InitiationHelpers.createMockCamundaVariables()));
    }

    private List<CamundaTask> createRetryableTasks() {
        ZonedDateTime now = ZonedDateTime.now();
        return List.of(
            new CamundaTask(
                "some id",
                "task name 1",
                "2151a580-c3c3-11eb-8b76-d26a7287fec2",
                "someAssignee",
                now,
                now.plusDays(1),
                "someCamundaTaskDescription",
                "someCamundaTaskOwner",
                "someCamundaTaskFormKey"
            ),
            new CamundaTask(
                "some other id",
                "task name 2",
                "2151a580-c3c3-11eb-8b76-d26a7287f000",
                "someAssignee",
                now,
                now.plusDays(1),
                "someCamundaTaskDescription",
                "someCamundaTaskOwner",
                "someCamundaTaskFormKey"
            )
        );
    }

    private List<CamundaTask> getTasksFromReport(GenericJobReport genericJobReport) {
        return genericJobReport.getOutcomeList().stream()
            .map(outcome -> new CamundaTask(outcome.getTaskId(), null, outcome.getProcessInstanceId()))
            .toList();
    }

    private void assertQuery(boolean timeFlag) throws JSONException {
        JSONObject query = new JSONObject(actualQueryParametersCaptor.getValue());
        if (timeFlag) {
            String createdAfter = query.getString("createdAfter");
            JSONAssert.assertEquals(
                getExpectedQueryParameters(createdAfter),
                actualQueryParametersCaptor.getValue(),
                JSONCompareMode.LENIENT
            );
        } else {
            JSONAssert.assertEquals(
                getExpectedQueryParameters(),
                actualQueryParametersCaptor.getValue(),
                JSONCompareMode.LENIENT
            );
        }
    }

    private void assertQueryTargetsUserTasksAndNotDelayedTasks() throws JSONException {
        JSONAssert.assertEquals(
            "{taskDefinitionKey: processTask}",
            actualQueryParametersCaptor.getValue(),
            JSONCompareMode.LENIENT
        );
    }

    @NotNull
    private String getExpectedQueryParameters(String createdAfter) {
        return "{\n"
               + "  \"orQueries\": [\n"
               + "    {\n"
               + "      \"taskVariables\": [\n"
               + "        {\n"
               + "          \"name\": \"cftTaskState\",\n"
               + "          \"operator\": \"eq\",\n"
               + "          \"value\": \"unconfigured\"\n"
               + "        }\n"
               + "      ]\n"
               + "    }\n"
               + "  ],\n"
               + " \"createdAfter\": \"" + createdAfter + "\",\n"
               + "  \"taskDefinitionKey\": \"processTask\",\n"
               + "  \"processDefinitionKey\": \"wa-task-initiation-ia-asylum\",\n"
               + "  \"sorting\": [\n"
               + "    {\n"
               + "      \"sortBy\": \"created\",\n"
               + "      \"sortOrder\": \"desc\"\n"
               + "    }\n"
               + "  ]"
               + "}\n";
    }

    @NotNull
    private String getExpectedQueryParameters() {
        return """
            {
              "orQueries": [
                {
                  "taskVariables": [
                    {
                      "name": "cftTaskState",
                      "operator": "eq",
                      "value": "unconfigured"
                    }
                  ]
                }
              ],
              "taskDefinitionKey": "processTask",
              "processDefinitionKey": "wa-task-initiation-ia-asylum",
              "sorting": [
                {
                  "sortBy": "created",
                  "sortOrder": "desc"
                }
              ]
            }""";
    }
}
