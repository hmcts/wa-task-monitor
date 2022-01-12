package uk.gov.hmcts.reform.wataskmonitor.services.jobs.termination;

import feign.FeignException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskManagementClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.TerminateTaskRequest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.options.TerminateInfo;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class TerminationJobServiceTest extends UnitBaseTest {
    public static final String CAMUNDA_DATE_REQUEST_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CAMUNDA_DATE_REQUEST_PATTERN);

    @Mock
    private CamundaClient camundaClient;

    @Mock
    private TaskManagementClient taskManagementClient;

    //@InjectMocks
    private TerminationJobService terminationJobService;

    @Captor
    private ArgumentCaptor<String> actualQueryParametersCaptor;

    @BeforeEach
    void setUp() {
        terminationJobService = new TerminationJobService(camundaClient,
                                                          taskManagementClient,
                                                          false,
                                                          120);
    }

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
    void should_handle_exception_when_call_to_task_management_fails() throws JSONException {

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
            eq("1000"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(expectedCamundaTasks);

        assertDoesNotThrow(() -> terminationJobService.terminateTasks(SOME_SERVICE_TOKEN));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void should_succeed_when_no_tasks_returned(int timeFlag) throws JSONException {
        terminationJobService = new TerminationJobService(camundaClient,
                                                          taskManagementClient,
                                                          timeFlag == 0 ? false : true,
                                                          120);
        when(camundaClient.getTasksFromHistory(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("1000"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(Collections.emptyList());

        terminationJobService.terminateTasks(SOME_SERVICE_TOKEN);

        assertQuery();
        verifyTerminateEndpointWasCalledWithTerminateReason("cancelled", 0);
        verifyTerminateEndpointWasCalledWithTerminateReason("completed", 0);
        verifyTerminateEndpointWasCalledWithTerminateReason("deleted", 0);
    }


    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void should_fetch_tasks_and_terminate_them(int timeFlag) throws JSONException {
        terminationJobService = new TerminationJobService(camundaClient,
                                                          taskManagementClient,
                                                          timeFlag == 0 ? false : true,
                                                          120);
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
        verifyTerminateEndpointWasCalledWithTerminateReason("cancelled", 1);
        verifyTerminateEndpointWasCalledWithTerminateReason("completed", 1);
        verifyTerminateEndpointWasCalledWithTerminateReason("deleted", 1);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void should_fetch_tasks_and_call_terminate_for_cancelled_task_only(int timeFlag) throws JSONException {
        terminationJobService = new TerminationJobService(camundaClient,
                                                          taskManagementClient,
                                                          timeFlag == 0 ? false : true,
                                                          120);
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
        verifyTerminateEndpointWasCalledWithTerminateReason("cancelled", 1);
        verifyTerminateEndpointWasCalledWithTerminateReason("completed", 0);
        verifyTerminateEndpointWasCalledWithTerminateReason("deleted", 0);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void should_fetch_tasks_and_call_terminate_for_completed_task_only(int timeFlag) throws JSONException {
        terminationJobService = new TerminationJobService(camundaClient,
                                                          taskManagementClient,
                                                          timeFlag == 0 ? false : true,
                                                          120);
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
        verifyTerminateEndpointWasCalledWithTerminateReason("cancelled", 0);
        verifyTerminateEndpointWasCalledWithTerminateReason("completed", 1);
        verifyTerminateEndpointWasCalledWithTerminateReason("deleted", 0);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void should_fetch_tasks_and_call_terminate_for_deleted_task_only(int timeFlag) throws JSONException {
        terminationJobService = new TerminationJobService(camundaClient,
                                                          taskManagementClient,
                                                          timeFlag == 0 ? false : true,
                                                          120);
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
        verifyTerminateEndpointWasCalledWithTerminateReason("cancelled", 0);
        verifyTerminateEndpointWasCalledWithTerminateReason("completed", 0);
        verifyTerminateEndpointWasCalledWithTerminateReason("deleted", 1);
    }

    private void verifyTerminateEndpointWasCalledWithTerminateReason(String terminateReason,
                                                                     int times) {
        TerminateTaskRequest request = new TerminateTaskRequest(new TerminateInfo(terminateReason));
        verify(taskManagementClient, times(times)).terminateTask(eq(SOME_SERVICE_TOKEN), any(), eq(request));
    }

    private void assertQuery() throws JSONException {
        JSONObject query = new JSONObject(actualQueryParametersCaptor.getValue());
        String finishedAfter = query.getString("finishedAfter");
        JSONAssert.assertEquals(
            getExpectedQueryParameters(finishedAfter),
            actualQueryParametersCaptor.getValue(),
            JSONCompareMode.LENIENT
        );
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
}

