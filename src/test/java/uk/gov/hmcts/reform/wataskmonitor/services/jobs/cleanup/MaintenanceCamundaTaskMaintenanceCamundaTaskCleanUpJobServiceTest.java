package uk.gov.hmcts.reform.wataskmonitor.services.jobs.cleanup;

import feign.FeignException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.config.job.CleanUpJobConfig;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.MAINTENANCE_CAMUNDA_TASK_CLEAN_UP;

@ExtendWith(OutputCaptureExtension.class)
class MaintenanceCamundaTaskMaintenanceCamundaTaskCleanUpJobServiceTest extends UnitBaseTest {

    @Mock
    private CamundaClient camundaClient;
    @Mock
    private CleanUpJobConfig cleanUpJobConfig;
    @Captor
    private ArgumentCaptor<String> actualQueryParametersCaptor;

    private MaintenanceCamundaTaskCleanUpJobService maintenanceCamundaTaskCleanUpJobService;

    @BeforeEach
    void setUp() {
        maintenanceCamundaTaskCleanUpJobService = new MaintenanceCamundaTaskCleanUpJobService(
            camundaClient,
            cleanUpJobConfig
        );
        lenient().when(cleanUpJobConfig.getCleanUpCamundaMaxResults()).thenReturn("50");
        lenient().when(cleanUpJobConfig.getCleanUpStartedDaysBefore()).thenReturn(7L);
        lenient().when(cleanUpJobConfig.getEnvironment()).thenReturn("aat");
        lenient().when(cleanUpJobConfig.getAllowedEnvironment()).thenReturn(List.of("local", "aat"));
    }

    @Test
    void when_no_tasks_exist_should_generate_report() {

        GenericJobReport actualActiveTaskReport = maintenanceCamundaTaskCleanUpJobService
            .deleteActiveProcesses(emptyList(), SOME_SERVICE_TOKEN);

        GenericJobReport actualHistoricTaskReport = maintenanceCamundaTaskCleanUpJobService
            .deleteHistoricProcesses(emptyList(), SOME_SERVICE_TOKEN);

        GenericJobReport expectation = new GenericJobReport(0, emptyList());
        assertEquals(expectation, actualActiveTaskReport);
        assertEquals(expectation, actualHistoricTaskReport);
    }

    @Test
    void when_environment_not_aat_should_return_empty_generate_report_for_active_tasks() {

        GenericJobReport actualActiveTaskReport = maintenanceCamundaTaskCleanUpJobService
            .deleteActiveProcesses(emptyList(), SOME_SERVICE_TOKEN);

        GenericJobReport expectation = new GenericJobReport(0, emptyList());
        assertEquals(expectation, actualActiveTaskReport);

    }

    @Test
    void when_environment_is_prod_should_return_empty_generate_report_for_active_tasks() {
        lenient().when(cleanUpJobConfig.getEnvironment()).thenReturn("prod");
        GenericJobReport actualActiveTaskReport = maintenanceCamundaTaskCleanUpJobService
            .deleteActiveProcesses(emptyList(), SOME_SERVICE_TOKEN);

        GenericJobReport expectation = new GenericJobReport(0, emptyList());
        assertEquals(expectation, actualActiveTaskReport);

    }

    @Test
    void should_retrieve_history_tasks(CapturedOutput output) throws JSONException {
        maintenanceCamundaTaskCleanUpJobService = new MaintenanceCamundaTaskCleanUpJobService(
            camundaClient,
            cleanUpJobConfig
        );

        HistoricCamundaTask camundaTask = new HistoricCamundaTask(
            "ac365ec0-4220-412e-bd0c-4cc56e71f64e",
            null,
            null,
            null
        );

        List<HistoricCamundaTask> tasks = singletonList(camundaTask);

        when(camundaClient.getHistoryProcesses(
            eq("0"),
            eq("50"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(tasks);

        List<HistoricCamundaTask> actualTaskList = maintenanceCamundaTaskCleanUpJobService.retrieveHistoricProcesses();

        verify(camundaClient, times(1))
            .getHistoryProcesses(anyString(), any(), any());

        assertEquals(tasks, actualTaskList);
        assertThat(output.getOut()).contains("cleanUpJobConfig:");
        assertThat(output.getOut()).contains("task(s) retrieved successfully from history");
        assertQueryForHistoric();

    }

    @Test
    void should_retrieve_active_processes(CapturedOutput output) throws JSONException {
        maintenanceCamundaTaskCleanUpJobService = new MaintenanceCamundaTaskCleanUpJobService(
            camundaClient,
            cleanUpJobConfig
        );

        HistoricCamundaTask camundaTask = new HistoricCamundaTask(
            "ac365ec0-4220-412e-bd0c-4cc56e71f64e",
            null,
            null,
            null
        );

        List<HistoricCamundaTask> tasks = singletonList(camundaTask);

        when(camundaClient.getHistoryProcesses(
            eq("0"),
            eq("50"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(tasks);

        List<HistoricCamundaTask> actualTaskList = maintenanceCamundaTaskCleanUpJobService.retrieveActiveProcesses();

        verify(camundaClient, times(1))
            .getHistoryProcesses(anyString(), any(), any());

        assertEquals(tasks, actualTaskList);
        assertThat(output.getOut()).contains("cleanUpJobConfig:");
        assertThat(output.getOut()).contains("active processes retrieved successfully");
        assertQueryForActive();

    }

    @Test
    void should_return_empty_list_when_environment_is_prod(CapturedOutput output) {
        maintenanceCamundaTaskCleanUpJobService = new MaintenanceCamundaTaskCleanUpJobService(
            camundaClient,
            cleanUpJobConfig
        );

        lenient().when(cleanUpJobConfig.getEnvironment()).thenReturn("prod");
        lenient().when(cleanUpJobConfig.getAllowedEnvironment()).thenReturn(List.of("local", "aat", "prod"));

        List<HistoricCamundaTask> actualTaskList = maintenanceCamundaTaskCleanUpJobService.retrieveHistoricProcesses();

        verify(camundaClient, never())
            .getHistoryProcesses(anyString(), any(), any());

        assertEquals(emptyList(), actualTaskList);
        assertThat(output.getOut()).contains("is not enabled for this environment: prod");

    }

    @Test
    void should_return_empty_list_when_environment_is_prod_for_active_processes(CapturedOutput output) {
        maintenanceCamundaTaskCleanUpJobService = new MaintenanceCamundaTaskCleanUpJobService(
            camundaClient,
            cleanUpJobConfig
        );

        lenient().when(cleanUpJobConfig.getEnvironment()).thenReturn("prod");
        lenient().when(cleanUpJobConfig.getAllowedEnvironment()).thenReturn(List.of("local", "aat", "prod"));

        List<HistoricCamundaTask> actualTaskList = maintenanceCamundaTaskCleanUpJobService.retrieveActiveProcesses();

        verify(camundaClient, never())
            .getHistoryProcesses(anyString(), any(), any());

        assertEquals(emptyList(), actualTaskList);
        assertThat(output.getOut()).contains("job can not be working on PROD environment");

    }

    @Test
    void should_delete_history_tasks() {
        maintenanceCamundaTaskCleanUpJobService = new MaintenanceCamundaTaskCleanUpJobService(
            camundaClient,
            cleanUpJobConfig
        );

        HistoricCamundaTask camundaTask = new HistoricCamundaTask(
            "ac365ec0-4220-412e-bd0c-4cc56e71f64e",
            null,
            null,
            null
        );

        List<HistoricCamundaTask> tasks = singletonList(camundaTask);

        GenericJobReport actualReport = maintenanceCamundaTaskCleanUpJobService
            .deleteHistoricProcesses(tasks, SOME_SERVICE_TOKEN);

        verify(camundaClient, times(1))
            .deleteHistoryProcesses(anyString(), anyString());

        GenericJobOutcome outcome = GenericJobOutcome.builder()
            .taskId(null)
            .processInstanceId(camundaTask.getId())
            .successful(true)
            .jobType(MAINTENANCE_CAMUNDA_TASK_CLEAN_UP.name())
            .build();

        GenericJobReport expectedReport = new GenericJobReport(1, singletonList(outcome));
        assertEquals(expectedReport, actualReport);

    }

    @Test
    void should_log_exception_when_an_error_occurred_in_delete_history_tasks(CapturedOutput output) {

        GenericJobReport expectedReport = new GenericJobReport(0, emptyList());
        List<HistoricCamundaTask> tasks = null;

        GenericJobReport actualReport = maintenanceCamundaTaskCleanUpJobService
            .deleteHistoricProcesses(tasks, SOME_SERVICE_TOKEN);

        assertEquals(expectedReport, actualReport);
        assertThat(output.getOut()).contains("There was no task(s) to delete.");
    }

    @Test
    void should_handle_exception_when_an_error_occurred_in_delete_history_tasks(CapturedOutput output) {

        HistoricCamundaTask camundaTask = new HistoricCamundaTask(
            "ac365ec0-4220-412e-bd0c-4cc56e71f64e",
            null,
            null,
            null
        );

        List<HistoricCamundaTask> tasks = singletonList(camundaTask);
        GenericJobOutcome outcome = GenericJobOutcome.builder()
            .taskId(null)
            .processInstanceId(camundaTask.getId())
            .successful(false)
            .jobType(MAINTENANCE_CAMUNDA_TASK_CLEAN_UP.name())
            .build();

        GenericJobReport expectedReport = new GenericJobReport(1, singletonList(outcome));

        doThrow(FeignException.class)
            .when(camundaClient)
            .deleteHistoryProcesses(
                eq(SOME_SERVICE_TOKEN),
                any()
            );


        GenericJobReport actualReport = maintenanceCamundaTaskCleanUpJobService
            .deleteHistoricProcesses(tasks, SOME_SERVICE_TOKEN);

        assertEquals(expectedReport, actualReport);
        assertThat(output.getOut()).contains("An error occurred when deleting history tasks :");
    }

    @Test
    void should_delete_active_tasks() {
        maintenanceCamundaTaskCleanUpJobService = new MaintenanceCamundaTaskCleanUpJobService(
            camundaClient,
            cleanUpJobConfig
        );

        HistoricCamundaTask camundaTask = new HistoricCamundaTask(
            "ac365ec0-4220-412e-bd0c-4cc56e71f64e",
            null,
            null,
            null
        );

        List<HistoricCamundaTask> tasks = singletonList(camundaTask);

        GenericJobReport actualReport = maintenanceCamundaTaskCleanUpJobService
            .deleteActiveProcesses(tasks, SOME_SERVICE_TOKEN);

        verify(camundaClient, times(1))
            .deleteActiveProcesses(anyString(), anyString());

        GenericJobOutcome outcome = GenericJobOutcome.builder()
            .taskId(null)
            .processInstanceId(camundaTask.getId())
            .successful(true)
            .jobType(MAINTENANCE_CAMUNDA_TASK_CLEAN_UP.name())
            .build();

        GenericJobReport expectedReport = new GenericJobReport(1, singletonList(outcome));
        assertEquals(expectedReport, actualReport);

        await().atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> assertThat(maintenanceCamundaTaskCleanUpJobService
                .deleteActiveProcesses(tasks, SOME_SERVICE_TOKEN))
                .isNotNull());

    }

    @Test
    void should_not_delete_active_tasks_when_environment_is_not_allowed() {
        lenient().when(cleanUpJobConfig.getEnvironment()).thenReturn("demo");

        maintenanceCamundaTaskCleanUpJobService = new MaintenanceCamundaTaskCleanUpJobService(
            camundaClient,
            cleanUpJobConfig
        );

        HistoricCamundaTask camundaTask = new HistoricCamundaTask(
            "ac365ec0-4220-412e-bd0c-4cc56e71f64e",
            null,
            null,
            null
        );

        List<HistoricCamundaTask> tasks = singletonList(camundaTask);

        GenericJobReport actualReport = maintenanceCamundaTaskCleanUpJobService
            .deleteActiveProcesses(tasks, SOME_SERVICE_TOKEN);

        GenericJobReport expectedReport = new GenericJobReport(0, emptyList());
        assertEquals(expectedReport, actualReport);

    }

    @Test
    void should_not_delete_active_tasks_when_environment_is_prod() {
        lenient().when(cleanUpJobConfig.getEnvironment()).thenReturn("prod");
        lenient().when(cleanUpJobConfig.getAllowedEnvironment()).thenReturn(List.of("local", "aat", "prod"));

        maintenanceCamundaTaskCleanUpJobService = new MaintenanceCamundaTaskCleanUpJobService(
            camundaClient,
            cleanUpJobConfig
        );

        HistoricCamundaTask camundaTask = new HistoricCamundaTask(
            "ac365ec0-4220-412e-bd0c-4cc56e71f64e",
            null,
            null,
            null
        );

        List<HistoricCamundaTask> tasks = singletonList(camundaTask);

        GenericJobReport actualReport = maintenanceCamundaTaskCleanUpJobService
            .deleteActiveProcesses(tasks, SOME_SERVICE_TOKEN);

        GenericJobReport expectedReport = new GenericJobReport(0, emptyList());

        verify(camundaClient, never())
            .deleteActiveProcesses(anyString(), any());

        assertEquals(expectedReport, actualReport);

    }

    @Test
    void should_log_exception_when_an_error_occurred_in_delete_active_tasks(CapturedOutput output) {

        GenericJobReport expectedReport = new GenericJobReport(0, emptyList());
        List<HistoricCamundaTask> tasks = null;

        GenericJobReport actualReport = maintenanceCamundaTaskCleanUpJobService
            .deleteActiveProcesses(tasks, SOME_SERVICE_TOKEN);

        assertEquals(expectedReport, actualReport);
        assertThat(output.getOut()).contains("There was no active task(s) to delete.");
    }

    @Test
    void should_handle_exception_when_an_error_occurred_in_delete_active_tasks(CapturedOutput output) {

        HistoricCamundaTask camundaTask = new HistoricCamundaTask(
            "ac365ec0-4220-412e-bd0c-4cc56e71f64e",
            null,
            null,
            null
        );

        List<HistoricCamundaTask> tasks = singletonList(camundaTask);
        GenericJobOutcome outcome = GenericJobOutcome.builder()
            .taskId(null)
            .processInstanceId(camundaTask.getId())
            .successful(false)
            .jobType(MAINTENANCE_CAMUNDA_TASK_CLEAN_UP.name())
            .build();

        GenericJobReport expectedReport = new GenericJobReport(1, singletonList(outcome));

        doThrow(FeignException.class)
            .when(camundaClient)
            .deleteActiveProcesses(
                eq(SOME_SERVICE_TOKEN),
                any()
            );


        GenericJobReport actualReport = maintenanceCamundaTaskCleanUpJobService
            .deleteActiveProcesses(tasks, SOME_SERVICE_TOKEN);

        assertEquals(expectedReport, actualReport);
        assertThat(output.getOut()).contains("An error occurred when deleting history tasks :");
    }

    @ParameterizedTest(name = "jobName: {0} expected: {1}")
    @CsvSource({
        "prod, false",
        "PROD, false",
        "local, true",
        "aat, true",
        "demo, false"
    })
    void should_return_a_boolean_according_to_environment(String environment, boolean expectedIsAllowedEnvironment) {

        when(cleanUpJobConfig.getEnvironment())
            .thenReturn(environment);

        boolean actualIsAllowedEnvironment = maintenanceCamundaTaskCleanUpJobService.isAllowedEnvironment();

        assertEquals(expectedIsAllowedEnvironment, actualIsAllowedEnvironment);

    }

    @ParameterizedTest(name = "jobName: {0} expected: {1}")
    @CsvSource({
        "local",
        "aat",
    })
    void should_log_a_message_when_environment_is_allowed(String environment, CapturedOutput output) {

        String enabledMessage = String.format("%s is enabled for this environment: %s",
            MAINTENANCE_CAMUNDA_TASK_CLEAN_UP.name(), environment);


        when(cleanUpJobConfig.getEnvironment())
            .thenReturn(environment);

        boolean isAllowedEnvironment = maintenanceCamundaTaskCleanUpJobService.isAllowedEnvironment();

        assertTrue(isAllowedEnvironment);

        assertThat(output.getOut()).contains(enabledMessage);

    }

    @ParameterizedTest(name = "jobName: {0} expected: {1}")
    @CsvSource({
        "prod",
        "demo",
        "dummy"
    })
    void should_log_a_message_when_environment_is_not_allowed(String environment, CapturedOutput output) {

        String enabledMessage = String.format("%s is not enabled for this environment: %s",
            MAINTENANCE_CAMUNDA_TASK_CLEAN_UP.name(), environment);


        when(cleanUpJobConfig.getEnvironment())
            .thenReturn(environment);

        boolean isAllowedEnvironment = maintenanceCamundaTaskCleanUpJobService.isAllowedEnvironment();

        assertFalse(isAllowedEnvironment);

        assertThat(output.getOut()).contains(enabledMessage);

    }

    @Test
    void should_return_false_even_if_allowed_environment_contains_prod() {

        lenient().when(cleanUpJobConfig.getEnvironment()).thenReturn("prod");
        lenient().when(cleanUpJobConfig.getAllowedEnvironment()).thenReturn(List.of("local", "aat", "prod"));

        boolean actualIsAllowedEnvironment = maintenanceCamundaTaskCleanUpJobService.isAllowedEnvironment();

        assertFalse(actualIsAllowedEnvironment);

    }

    private void assertQueryForHistoric() throws JSONException {
        JSONObject query = new JSONObject(actualQueryParametersCaptor.getValue());

        String startedBefore = query.getString("startedBefore");

        assertDoesNotThrow(() -> ZonedDateTime.parse(startedBefore, maintenanceCamundaTaskCleanUpJobService.formatter));

        JSONAssert.assertEquals(
            getExpectedQueryParametersForHistoric(startedBefore),
            actualQueryParametersCaptor.getValue(),
            JSONCompareMode.LENIENT
        );

    }

    private void assertQueryForActive() throws JSONException {
        JSONObject query = new JSONObject(actualQueryParametersCaptor.getValue());

        String startedBefore = query.getString("startedBefore");

        assertDoesNotThrow(() -> ZonedDateTime.parse(startedBefore, maintenanceCamundaTaskCleanUpJobService.formatter));

        JSONAssert.assertEquals(
            getExpectedQueryParametersForActive(startedBefore),
            actualQueryParametersCaptor.getValue(),
            JSONCompareMode.LENIENT
        );

    }

    @NotNull
    private String getExpectedQueryParametersForHistoric(String startedBefore) {

        return String.format("""
             {
                 "processDefinitionKey": "wa-task-initiation-ia-asylum",
                 "startedBefore": "%s",
                 "finished": true
             }
            """, startedBefore);

    }

    @NotNull
    private String getExpectedQueryParametersForActive(String startedBefore) {
        return String.format("""
            {
                "processDefinitionKey": "wa-task-initiation-ia-asylum",
                "startedBefore": "%s",
                "unfinished": true
            }
            """, startedBefore);

    }

}
