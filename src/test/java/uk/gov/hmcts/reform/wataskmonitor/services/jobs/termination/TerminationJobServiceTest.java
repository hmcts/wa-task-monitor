package uk.gov.hmcts.reform.wataskmonitor.services.jobs.termination;

import feign.FeignException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskManagementClient;
import uk.gov.hmcts.reform.wataskmonitor.config.entity.Migration;
import uk.gov.hmcts.reform.wataskmonitor.config.job.TerminationJobConfig;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.TerminateTaskRequest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.options.TerminateInfo;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class TerminationJobServiceTest extends UnitBaseTest {

    @Mock
    private CamundaClient camundaClient;

    @Mock
    private TaskManagementClient taskManagementClient;

    private TerminationJobService terminationJobService;

    @Captor
    private ArgumentCaptor<String> actualQueryParametersCaptor;

    @Mock
    private TerminationJobConfig terminationJobConfig;

    @BeforeEach
    void setUp() {
        terminationJobService = new TerminationJobService(
            camundaClient,
            taskManagementClient,
            terminationJobConfig
        );
        when(terminationJobConfig.getCamundaMaxResults()).thenReturn("100");
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

        assertThatThrownBy(() -> terminationJobService.terminateTasks(SOME_SERVICE_TOKEN))
            .isInstanceOf(FeignException.class)
            .hasNoCause();
    }


    @Test
    void should_handle_exception_when_call_to_task_management_fails() {

        doThrow(FeignException.GatewayTimeout.class)
            .when(taskManagementClient)
            .terminateTask(
                eq(SOME_SERVICE_TOKEN),
                any(),
                any(TerminateTaskRequest.class)
            );

        List<HistoricCamundaTask> expectedCamundaTasks = List.of(
            new HistoricCamundaTask("1", "cancelled"),
            new HistoricCamundaTask("2", "completed"),
            new HistoricCamundaTask("3", "deleted")
        );

        when(camundaClient.getTasksFromHistory(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(expectedCamundaTasks);

        assertDoesNotThrow(() -> terminationJobService.terminateTasks(SOME_SERVICE_TOKEN));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void should_succeed_when_no_tasks_returned(boolean timeFlag) throws JSONException {
        terminationJobService = new TerminationJobService(
            camundaClient,
            taskManagementClient,
            terminationJobConfig
        );
        when(camundaClient.getTasksFromHistory(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(Collections.emptyList());

        terminationJobService.terminateTasks(SOME_SERVICE_TOKEN);

        assertQuery(timeFlag);
        verifyTerminateEndpointWasCalledWithTerminateReason("cancelled", 0);
        verifyTerminateEndpointWasCalledWithTerminateReason("completed", 0);
        verifyTerminateEndpointWasCalledWithTerminateReason("deleted", 0);
    }


    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void should_fetch_tasks_and_terminate_them(boolean timeFlag) throws JSONException {
        terminationJobService = new TerminationJobService(
            camundaClient,
            taskManagementClient,
            terminationJobConfig
        );
        List<HistoricCamundaTask> expectedCamundaTasks = List.of(
            new HistoricCamundaTask("1", "cancelled"),
            new HistoricCamundaTask("2", "completed"),
            new HistoricCamundaTask("3", "deleted")
        );

        when(camundaClient.getTasksFromHistory(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(expectedCamundaTasks);

        terminationJobService.terminateTasks(SOME_SERVICE_TOKEN);

        assertQuery(timeFlag);
        verifyTerminateEndpointWasCalledWithTerminateReason("cancelled", 1);
        verifyTerminateEndpointWasCalledWithTerminateReason("completed", 1);
        verifyTerminateEndpointWasCalledWithTerminateReason("deleted", 1);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void should_fetch_tasks_and_call_terminate_for_cancelled_task_only(boolean timeFlag) throws JSONException {
        terminationJobService = new TerminationJobService(
            camundaClient,
            taskManagementClient,
            terminationJobConfig
        );
        List<HistoricCamundaTask> expectedCamundaTasks = List.of(
            new HistoricCamundaTask("1", "cancelled")
        );

        when(camundaClient.getTasksFromHistory(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(expectedCamundaTasks);

        terminationJobService.terminateTasks(SOME_SERVICE_TOKEN);

        assertQuery(timeFlag);
        verifyTerminateEndpointWasCalledWithTerminateReason("cancelled", 1);
        verifyTerminateEndpointWasCalledWithTerminateReason("completed", 0);
        verifyTerminateEndpointWasCalledWithTerminateReason("deleted", 0);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void should_fetch_tasks_and_call_terminate_for_completed_task_only(boolean timeFlag) throws JSONException {
        terminationJobService = new TerminationJobService(
            camundaClient,
            taskManagementClient,
            terminationJobConfig
        );

        List<HistoricCamundaTask> expectedCamundaTasks = List.of(
            new HistoricCamundaTask("1", "completed")
        );

        when(terminationJobConfig.getCamundaMaxResults()).thenReturn("100");

        when(camundaClient.getTasksFromHistory(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(expectedCamundaTasks);

        terminationJobService.terminateTasks(SOME_SERVICE_TOKEN);

        assertQuery(timeFlag);
        verifyTerminateEndpointWasCalledWithTerminateReason("cancelled", 0);
        verifyTerminateEndpointWasCalledWithTerminateReason("completed", 1);
        verifyTerminateEndpointWasCalledWithTerminateReason("deleted", 0);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void should_fetch_tasks_and_call_terminate_for_deleted_task_only(boolean timeFlag) throws JSONException {
        terminationJobService = new TerminationJobService(
            camundaClient,
            taskManagementClient,
            terminationJobConfig
        );

        lenient().when(terminationJobConfig.isCamundaTimeLimitFlag()).thenReturn(timeFlag);

        List<HistoricCamundaTask> expectedCamundaTasks = List.of(
            new HistoricCamundaTask("1", "deleted")
        );

        when(camundaClient.getTasksFromHistory(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(expectedCamundaTasks);

        terminationJobService.terminateTasks(SOME_SERVICE_TOKEN);

        assertEquals(timeFlag, terminationJobService.isTerminationTimeLimitFlag());
        assertEquals(120, terminationJobService.getTerminationTimeLimit());
        assertQuery(timeFlag);
        verifyTerminateEndpointWasCalledWithTerminateReason("cancelled", 0);
        verifyTerminateEndpointWasCalledWithTerminateReason("completed", 0);
        verifyTerminateEndpointWasCalledWithTerminateReason("deleted", 1);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void should_createdAfter_exists_or_not_in_query_according_to_termination_flag(
        boolean timeFlag) throws JSONException {

        List<HistoricCamundaTask> expectedCamundaTasks = List.of(
            new HistoricCamundaTask("1", "deleted")
        );

        when(terminationJobConfig.getCamundaMaxResults()).thenReturn("100");

        when(camundaClient.getTasksFromHistory(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(expectedCamundaTasks);

        terminationJobService = new TerminationJobService(
            camundaClient,
            taskManagementClient,
            terminationJobConfig
        );

        terminationJobService.terminateTasks(SOME_SERVICE_TOKEN);

        assertQuery(timeFlag);

    }

    @ParameterizedTest
    @CsvSource({
        "false, 100, true",
        "true, 1, false"
    })
    void should_fetch_tasks_and_call_terminate_for_deleted_task_only_according_to_migration_flag(
        boolean migrationFlag, String camundaMaxResult, boolean timeFlag) {
        terminationJobService = new TerminationJobService(
            camundaClient,
            taskManagementClient,
            terminationJobConfig
        );

        Migration migration = spy(Migration.class);
        lenient().when(terminationJobConfig.getMigration()).thenReturn(migration);
        lenient().when(migration.isMigrationFlag()).thenReturn(migrationFlag);
        lenient().when(migration.getCamundaMaxResults()).thenReturn(camundaMaxResult);
        lenient().when(migration.isMigrationFlag()).thenReturn(migrationFlag);

        lenient().when(terminationJobConfig.isCamundaTimeLimitFlag()).thenReturn(timeFlag);
        lenient().when(terminationJobConfig.getCamundaMaxResults()).thenReturn(camundaMaxResult);

        List<HistoricCamundaTask> expectedCamundaTasks = List.of(
            new HistoricCamundaTask("1", "deleted")
        );

        when(camundaClient.getTasksFromHistory(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq(camundaMaxResult),
            actualQueryParametersCaptor.capture()
        )).thenReturn(expectedCamundaTasks);

        terminationJobService.terminateTasks(SOME_SERVICE_TOKEN);

        assertEquals(timeFlag, terminationJobService.isTerminationTimeLimitFlag());
        assertEquals(120, terminationJobService.getTerminationTimeLimit());
        assertEquals(camundaMaxResult, terminationJobService.getMaxResults());

    }

    private void assertQuery(boolean timeFlag) throws JSONException {
        JSONObject query = new JSONObject(actualQueryParametersCaptor.getValue());
        if (timeFlag) {
            String createdAfter = query.getString("finishedAfter");
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

    private void verifyTerminateEndpointWasCalledWithTerminateReason(String terminateReason,
                                                                     int times) {
        TerminateTaskRequest request = new TerminateTaskRequest(new TerminateInfo(terminateReason));
        verify(taskManagementClient, times(times)).terminateTask(eq(SOME_SERVICE_TOKEN), any(), eq(request));
    }

    @NotNull
    private String getExpectedQueryParameters(String finishedAfter) {
        return "{\n"
               + "  \"taskVariables\": [\n"
               + "    {\n"
               + "      \"name\": \"cftTaskState\",\n"
               + "      \"operator\": \"eq\",\n"
               + "      \"value\": \"pendingTermination\"\n"
               + "    }\n"
               + "  ],\n"
               + " \"finishedAfter\": \"" + finishedAfter + "\",\n"
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

