package uk.gov.hmcts.reform.wataskmonitor.services.jobs.failure.termination;

import feign.FeignException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.config.job.TerminationJobConfig;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@ExtendWith(OutputCaptureExtension.class)
class TaskTerminationFailuresJobServiceTest extends UnitBaseTest {

    @Mock
    private CamundaClient camundaClient;

    private TaskTerminationFailuresJobService taskTerminationFailuresJobService;

    @Captor
    private ArgumentCaptor<String> actualQueryParametersCaptor;

    @Mock
    private TerminationJobConfig terminationJobConfig;

    @BeforeEach
    void setUp() {
        taskTerminationFailuresJobService = new TaskTerminationFailuresJobService(
            camundaClient,
            terminationJobConfig
        );
        lenient().when(terminationJobConfig.getCamundaMaxResults()).thenReturn("100");
        lenient().when(terminationJobConfig.isCamundaTimeLimitFlag()).thenReturn(true);
        lenient().when(terminationJobConfig.getCamundaTimeLimit()).thenReturn(120L);
    }

    @Test
    void should_throw_exception_when_camunda_call_fails() {

        doThrow(FeignException.GatewayTimeout.class)
            .when(camundaClient)
            .getTasksFromHistory(
                eq(SOME_SERVICE_TOKEN),
                eq("0"),
                eq("100"),
                any()
            );

        assertThatThrownBy(() -> taskTerminationFailuresJobService.checkUnTerminatedTasks(SOME_SERVICE_TOKEN))
            .isInstanceOf(FeignException.class)
            .hasNoCause();
    }

    @Test
    void should_succeed_when_no_tasks_returned() throws JSONException {
        taskTerminationFailuresJobService = new TaskTerminationFailuresJobService(
            camundaClient,
            terminationJobConfig
        );
        lenient().when(terminationJobConfig.isCamundaTimeLimitFlag()).thenReturn(true);

        when(camundaClient.getTasksFromHistory(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(emptyList());

        taskTerminationFailuresJobService.checkUnTerminatedTasks(SOME_SERVICE_TOKEN);

        assertQuery();

    }

    @Test
    void should_fetch_tasks() throws JSONException {
        taskTerminationFailuresJobService = new TaskTerminationFailuresJobService(
            camundaClient,
            terminationJobConfig
        );

        List<HistoricCamundaTask> expectedCamundaTasks = List.of(
            new HistoricCamundaTask("1", "cancelled", null, null),
            new HistoricCamundaTask("2", "completed", null, null),
            new HistoricCamundaTask("3", "deleted", null, null)
        );

        when(camundaClient.getTasksFromHistory(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(expectedCamundaTasks);

        taskTerminationFailuresJobService.checkUnTerminatedTasks(SOME_SERVICE_TOKEN);

        assertQuery();

    }

    @Test
    void should_not_create_alert_if_time_limit_flag_is_false() {
        taskTerminationFailuresJobService = new TaskTerminationFailuresJobService(
            camundaClient,
            terminationJobConfig
        );

        lenient().when(terminationJobConfig.isCamundaTimeLimitFlag()).thenReturn(false);

        taskTerminationFailuresJobService.checkUnTerminatedTasks(SOME_SERVICE_TOKEN);

        verify(camundaClient, never()).getTasksFromHistory(any(), any(), any(), any());
    }

    @Test
    void should_log_message_when_no_unterminated_task_found(CapturedOutput output) {
        taskTerminationFailuresJobService = new TaskTerminationFailuresJobService(
            camundaClient,
            terminationJobConfig
        );

        when(camundaClient.getTasksFromHistory(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(emptyList());

        taskTerminationFailuresJobService.checkUnTerminatedTasks(SOME_SERVICE_TOKEN);

        assertThat(output.getOut().contains("TASK_TERMINATION_FAILURES There was no task"));
    }

    @Test
    void should_log_message_when_unterminated_task_found(CapturedOutput output) {
        taskTerminationFailuresJobService = new TaskTerminationFailuresJobService(
            camundaClient,
            terminationJobConfig
        );

        List<HistoricCamundaTask> expectedCamundaTasks = List.of(
            new HistoricCamundaTask("1", "cancelled", null, null),
            new HistoricCamundaTask("2", "completed", null, null),
            new HistoricCamundaTask("3", "deleted", null, null)
        );

        when(camundaClient.getTasksFromHistory(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(expectedCamundaTasks);

        taskTerminationFailuresJobService.checkUnTerminatedTasks(SOME_SERVICE_TOKEN);

        assertThat(output.getOut().contains("TASK_TERMINATION_FAILURES -> taskId:1"));
        assertThat(output.getOut().contains("TASK_TERMINATION_FAILURES -> taskId:2"));
        assertThat(output.getOut().contains("TASK_TERMINATION_FAILURES -> taskId:3"));
        assertThat(output.getOut().contains("TASK_TERMINATION_FAILURES There are some unterminated tasks"));
    }

    private void assertQuery() throws JSONException {
        JSONObject query = new JSONObject(actualQueryParametersCaptor.getValue());
        String finishedBefore = query.getString("finishedBefore");
        JSONAssert.assertEquals(
            getExpectedQueryParameters(finishedBefore),
            actualQueryParametersCaptor.getValue(),
            JSONCompareMode.LENIENT
        );
    }

    @NotNull
    private String getExpectedQueryParameters(String finishedBefore) {
        return "{\n"
               + "  \"taskVariables\": [\n"
               + "    {\n"
               + "      \"name\": \"cftTaskState\",\n"
               + "      \"operator\": \"eq\",\n"
               + "      \"value\": \"pendingTermination\"\n"
               + "    }\n"
               + "  ],\n"
               + " \"finishedBefore\": \"" + finishedBefore + "\",\n"
               + "  \"taskDefinitionKey\": \"processTask\",\n"
               + "  \"processDefinitionKey\": \"wa-task-initiation-ia-asylum\",\n"
               + "  \"sorting\": [\n"
               + "    {\n"
               + "      \"sortBy\": \"endTime\",\n"
               + "      \"sortOrder\": \"desc\"\n"
               + "    }\n"
               + "  ]"
               + "}";
    }

    @NotNull
    private String getExpectedQueryParameters() {
        return "{\n"
               + "  \"taskVariables\": [\n"
               + "    {\n"
               + "      \"name\": \"cftTaskState\",\n"
               + "      \"operator\": \"eq\",\n"
               + "      \"value\": \"pendingTermination\"\n"
               + "    }\n"
               + "  ],\n"
               + "  \"taskDefinitionKey\": \"processTask\",\n"
               + "  \"processDefinitionKey\": \"wa-task-initiation-ia-asylum\",\n"
               + "  \"sorting\": [\n"
               + "    {\n"
               + "      \"sortBy\": \"endTime\",\n"
               + "      \"sortOrder\": \"desc\"\n"
               + "    }\n"
               + "  ]"
               + "}";
    }
}

