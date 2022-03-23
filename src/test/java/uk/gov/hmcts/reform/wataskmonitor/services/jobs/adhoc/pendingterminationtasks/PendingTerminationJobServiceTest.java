package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.pendingterminationtasks;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.config.job.PendingTerminationJobConfig;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoryVariableInstance;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.CFT_TASK_STATE;

class PendingTerminationJobServiceTest extends UnitBaseTest {

    private static final String taskId = "task_id";

    @Mock
    private CamundaClient camundaClient;

    @Mock
    private PendingTerminationJobConfig pendingTerminationJobConfig;

    private PendingTerminationJobService pendingTerminationJobService;

    @BeforeEach
    void setUp() {
        pendingTerminationJobService = new PendingTerminationJobService(
            camundaClient,
            pendingTerminationJobConfig
        );
        when(pendingTerminationJobConfig.getCamundaMaxResults()).thenReturn("1");
    }

    @Test
    void should_throw_exception_when_camunda_call_fails_for_task_from_history() {

        doThrow(FeignException.GatewayTimeout.class)
            .when(camundaClient)
            .getTasksFromHistory(
                eq(SOME_SERVICE_TOKEN),
                eq("0"),
                eq("1"),
                any()
            );

        assertThatThrownBy(() -> pendingTerminationJobService.terminateTasks(SOME_SERVICE_TOKEN))
            .isInstanceOf(FeignException.class)
            .hasNoCause();
    }

    @Test
    void should_handle_exception_when_camunda_call_fails_for_search_history() {

        doReturn(List.of(new HistoricCamundaTask("task-id-2", "delete reason"),
                         new HistoricCamundaTask(taskId, "delete reason")))
            .when(camundaClient)
            .getTasksFromHistory(
                eq(SOME_SERVICE_TOKEN),
                eq("0"),
                eq("1"),
                any()
            );

        doReturn(List.of(new HistoryVariableInstance(taskId, CFT_TASK_STATE.value(), "pendingTermination")))
            .when(camundaClient)
            .searchHistory(
                SOME_SERVICE_TOKEN,
                Map.of("variableName", CFT_TASK_STATE.value(),"taskIdIn", singleton(taskId))
            );

        doThrow(FeignException.GatewayTimeout.class)
            .when(camundaClient)
            .searchHistory(
                SOME_SERVICE_TOKEN,
                Map.of("variableName", CFT_TASK_STATE.value(),"taskIdIn", singleton("task-id-2"))
            );

        pendingTerminationJobService.terminateTasks(SOME_SERVICE_TOKEN);

        verify(camundaClient).deleteVariableFromHistory(SOME_SERVICE_TOKEN, taskId);
    }

    @Test
    void should_handle_exception_when_camunda_call_fails_for_delete_variable_from_history() {

        doReturn(List.of(new HistoricCamundaTask("task-id-2", "delete reason"),
                         new HistoricCamundaTask(taskId, "delete reason")))
            .when(camundaClient)
            .getTasksFromHistory(
                eq(SOME_SERVICE_TOKEN),
                eq("0"),
                eq("1"),
                any()
            );

        doReturn(List.of(new HistoryVariableInstance(taskId, CFT_TASK_STATE.value(), "pendingTermination")))
            .when(camundaClient)
            .searchHistory(
                SOME_SERVICE_TOKEN,
                Map.of("variableName", CFT_TASK_STATE.value(),"taskIdIn", singleton(taskId))
            );

        doThrow(FeignException.GatewayTimeout.class)
            .when(camundaClient)
            .deleteVariableFromHistory(
                SOME_SERVICE_TOKEN,
                "task-id-2"
            );

        pendingTerminationJobService.terminateTasks(SOME_SERVICE_TOKEN);

        verify(camundaClient).deleteVariableFromHistory(SOME_SERVICE_TOKEN, taskId);
    }

    @Test
    void should_call_delete_variable_task_when_camunda_return_variable_from_history() {

        doReturn(List.of(new HistoricCamundaTask(taskId, "delete reason")))
            .when(camundaClient)
            .getTasksFromHistory(
                eq(SOME_SERVICE_TOKEN),
                eq("0"),
                eq("1"),
                any()
            );

        doReturn(List.of(new HistoryVariableInstance(taskId, CFT_TASK_STATE.value(), "pendingTermination")))
            .when(camundaClient)
            .searchHistory(
                SOME_SERVICE_TOKEN,
                Map.of("variableName", CFT_TASK_STATE.value(),"taskIdIn", singleton(taskId))
            );

        pendingTerminationJobService.terminateTasks(SOME_SERVICE_TOKEN);

        verify(camundaClient).deleteVariableFromHistory(SOME_SERVICE_TOKEN, taskId);
    }

}
