package uk.gov.hmcts.reform.wataskmonitor.services.jobs.termination;

import feign.FeignException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskManagementClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.TerminateTaskRequest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TerminateReason;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.options.TerminateInfo;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TerminateReason.CANCELLED;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TerminateReason.COMPLETED;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TerminateReason.DELETED;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class TerminationJobServiceTest extends UnitBaseTest {

    @Mock
    private CamundaClient camundaClient;

    @Mock
    private TaskManagementClient taskManagementClient;

    @InjectMocks
    private TerminationJobService terminationJobService;
    @Captor
    private ArgumentCaptor<String> actualQueryParametersCaptor;

    @Test
    void should_throw_exception_when_camunda_call_fails() {

        doThrow(FeignException.GatewayTimeout.class)
            .when(camundaClient)
            .getTasksFromHistory(
                eq(SOME_SERVICE_TOKEN),
                eq("0"),
                eq("1000"),
                any()
            );

        assertThatThrownBy(() -> terminationJobService.terminateTasks(SOME_SERVICE_TOKEN))
            .isInstanceOf(FeignException.class)
            .hasNoCause();
    }


    @Test
    void should_succeed_when_no_tasks_returned() throws JSONException {

        when(camundaClient.getTasksFromHistory(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("1000"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(Collections.emptyList());

        terminationJobService.terminateTasks(SOME_SERVICE_TOKEN);

        assertQuery();
        verifyTerminateEndpointWasCalledWithTerminateReason(CANCELLED, 0);
        verifyTerminateEndpointWasCalledWithTerminateReason(COMPLETED, 0);
        verifyTerminateEndpointWasCalledWithTerminateReason(DELETED, 0);
    }


    @Test
    void should_fetch_tasks_and_terminate_them() throws JSONException {
        List<HistoricCamundaTask> expectedCamundaTasks = List.of(
            new HistoricCamundaTask("1", "cancelled"),
            new HistoricCamundaTask("2", "completed"),
            new HistoricCamundaTask("3", "deleted")
        );

        when(camundaClient.getTasksFromHistory(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("1000"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(expectedCamundaTasks);

        terminationJobService.terminateTasks(SOME_SERVICE_TOKEN);

        assertQuery();
        verifyTerminateEndpointWasCalledWithTerminateReason(CANCELLED, 1);
        verifyTerminateEndpointWasCalledWithTerminateReason(COMPLETED, 1);
        verifyTerminateEndpointWasCalledWithTerminateReason(DELETED, 1);
    }


    @Test
    void should_fetch_tasks_and_call_terminate_for_cancelled_task_only() throws JSONException {
        List<HistoricCamundaTask> expectedCamundaTasks = List.of(
            new HistoricCamundaTask("1", "cancelled")
        );

        when(camundaClient.getTasksFromHistory(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("1000"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(expectedCamundaTasks);

        terminationJobService.terminateTasks(SOME_SERVICE_TOKEN);

        assertQuery();
        verifyTerminateEndpointWasCalledWithTerminateReason(CANCELLED, 1);
        verifyTerminateEndpointWasCalledWithTerminateReason(COMPLETED, 0);
        verifyTerminateEndpointWasCalledWithTerminateReason(DELETED, 0);
    }

    @Test
    void should_fetch_tasks_and_call_terminate_for_completed_task_only() throws JSONException {
        List<HistoricCamundaTask> expectedCamundaTasks = List.of(
            new HistoricCamundaTask("1", "completed")
        );

        when(camundaClient.getTasksFromHistory(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("1000"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(expectedCamundaTasks);

        terminationJobService.terminateTasks(SOME_SERVICE_TOKEN);

        assertQuery();
        verifyTerminateEndpointWasCalledWithTerminateReason(CANCELLED, 0);
        verifyTerminateEndpointWasCalledWithTerminateReason(COMPLETED, 1);
        verifyTerminateEndpointWasCalledWithTerminateReason(DELETED, 0);
    }

    @Test
    void should_fetch_tasks_and_call_terminate_for_deleted_task_only() throws JSONException {
        List<HistoricCamundaTask> expectedCamundaTasks = List.of(
            new HistoricCamundaTask("1", "deleted")
        );

        when(camundaClient.getTasksFromHistory(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("1000"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(expectedCamundaTasks);

        terminationJobService.terminateTasks(SOME_SERVICE_TOKEN);

        assertQuery();
        verifyTerminateEndpointWasCalledWithTerminateReason(CANCELLED, 0);
        verifyTerminateEndpointWasCalledWithTerminateReason(COMPLETED, 0);
        verifyTerminateEndpointWasCalledWithTerminateReason(DELETED, 1);
    }

    private void verifyTerminateEndpointWasCalledWithTerminateReason(TerminateReason terminateReason,
                                                                     int times) {
        TerminateTaskRequest request = new TerminateTaskRequest(new TerminateInfo(terminateReason));
        verify(taskManagementClient, times(times)).terminateTask(eq(SOME_SERVICE_TOKEN), any(), eq(request));
    }

    private void assertQuery() throws JSONException {
        JSONAssert.assertEquals(
            getExpectedQueryParameters(),
            actualQueryParametersCaptor.getValue(),
            JSONCompareMode.LENIENT
        );
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
               + "  \"processDefinitionKey\": \"wa-task-initiation-ia-asylum\"\n"
               + "}";
    }
}

