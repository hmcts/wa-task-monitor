package uk.gov.hmcts.reform.wataskmonitor.services.jobs.cleanup;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTaskCount;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.TASK_CLEAN_UP;

@ExtendWith(OutputCaptureExtension.class)
class CleanUpJobServiceTest extends UnitBaseTest {

    @Mock
    private CamundaClient camundaClient;
    @Mock
    private CleanUpJobConfig cleanUpJobConfig;
    @Captor
    private ArgumentCaptor<String> actualQueryParametersCaptor;

    private CleanUpJobService cleanUpJobService;

    @BeforeEach
    void setUp() {
        cleanUpJobService = new CleanUpJobService(
            camundaClient,
            cleanUpJobConfig,
            new ObjectMapper()
        );
        lenient().when(cleanUpJobConfig.getCamundaMaxResults()).thenReturn("50");
        lenient().when(cleanUpJobConfig.getStartedBeforeDays()).thenReturn(7L);
        lenient().when(cleanUpJobConfig.getEnvironment()).thenReturn("aat");
    }

    @Test
    void when_no_tasks_exist_should_generate_report() {

        GenericJobReport actualActiveTaskReport = cleanUpJobService
            .deleteActiveProcesses(emptyList(), SOME_SERVICE_TOKEN);

        GenericJobReport actualHistoricTaskReport = cleanUpJobService
            .deleteHistoricProcesses(emptyList(), SOME_SERVICE_TOKEN);

        GenericJobReport expectation = new GenericJobReport(0, emptyList());
        assertEquals(expectation, actualActiveTaskReport);
        assertEquals(expectation, actualHistoricTaskReport);
    }

    @Test
    void when_environment_not_aat_should_return_empty_generate_report_for_active_tasks() {

        GenericJobReport actualActiveTaskReport = cleanUpJobService
            .deleteActiveProcesses(emptyList(), SOME_SERVICE_TOKEN);

        GenericJobReport expectation = new GenericJobReport(0, emptyList());
        assertEquals(expectation, actualActiveTaskReport);

    }

    @Test
    void should_retrieve_history_tasks(CapturedOutput output) throws JSONException {
        cleanUpJobService = new CleanUpJobService(
            camundaClient,
            cleanUpJobConfig,
            new ObjectMapper()
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

        List<HistoricCamundaTask> actualTaskList = cleanUpJobService.retrieveProcesses();

        verify(camundaClient, times(1))
            .getHistoryProcesses(anyString(), any(), any());

        assertEquals(tasks, actualTaskList);
        assertThat(output.getOut().contains("cleanUpJobConfig:"));
        assertThat(output.getOut().contains("task(s) retrieved successfully from history"));
        assertQuery();

    }

    @Test
    void should_delete_history_tasks() {
        cleanUpJobService = new CleanUpJobService(
            camundaClient,
            cleanUpJobConfig,
            new ObjectMapper()
        );

        HistoricCamundaTask camundaTask = new HistoricCamundaTask(
            "ac365ec0-4220-412e-bd0c-4cc56e71f64e",
            null,
            null,
            null
        );

        List<HistoricCamundaTask> tasks = singletonList(camundaTask);

        when(camundaClient.getHistoryProcessCount(any()))
            .thenReturn(new CamundaTaskCount(1L));

        GenericJobReport actualReport = cleanUpJobService.deleteHistoricProcesses(tasks, SOME_SERVICE_TOKEN);

        verify(camundaClient, times(2))
            .getHistoryProcessCount(anyString());

        verify(camundaClient, times(1))
            .deleteHistoryProcesses(anyString(), anyString());

        GenericJobOutcome outcome = GenericJobOutcome.builder()
            .taskId(null)
            .processInstanceId(camundaTask.getId())
            .successful(true)
            .jobType(TASK_CLEAN_UP.name())
            .build();

        GenericJobReport expectedReport = new GenericJobReport(1, singletonList(outcome));
        assertEquals(expectedReport, actualReport);

    }

    @Test
    void should_log_exception_when_an_error_occurred_in_delete_history_tasks(CapturedOutput output) {

        GenericJobReport expectedReport = new GenericJobReport(0, emptyList());
        List<HistoricCamundaTask> tasks = null;

        GenericJobReport actualReport = cleanUpJobService.deleteHistoricProcesses(tasks, SOME_SERVICE_TOKEN);

        assertEquals(expectedReport, actualReport);
        assertThat(output.getOut().contains("There was no task(s) to delete."));
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
            .jobType(TASK_CLEAN_UP.name())
            .build();

        GenericJobReport expectedReport = new GenericJobReport(1, singletonList(outcome));

        when(camundaClient.getHistoryProcessCount(any()))
            .thenReturn(new CamundaTaskCount(1L));

        doThrow(FeignException.class)
            .when(camundaClient)
            .deleteHistoryProcesses(
                eq(SOME_SERVICE_TOKEN),
                any()
            );


        GenericJobReport actualReport = cleanUpJobService.deleteHistoricProcesses(tasks, SOME_SERVICE_TOKEN);

        assertEquals(expectedReport, actualReport);
        assertThat(output.getOut().contains("An error occurred when deleting history tasks :"));
    }

    @Test
    void should_delete_active_tasks() {
        cleanUpJobService = new CleanUpJobService(
            camundaClient,
            cleanUpJobConfig,
            new ObjectMapper()
        );

        HistoricCamundaTask camundaTask = new HistoricCamundaTask(
            "ac365ec0-4220-412e-bd0c-4cc56e71f64e",
            null,
            null,
            null
        );

        List<HistoricCamundaTask> tasks = singletonList(camundaTask);

        when(camundaClient.getActiveProcessCount(any()))
            .thenReturn(new CamundaTaskCount(1L));

        GenericJobReport actualReport = cleanUpJobService.deleteActiveProcesses(tasks, SOME_SERVICE_TOKEN);

        verify(camundaClient, times(2))
            .getActiveProcessCount(anyString());

        verify(camundaClient, times(1))
            .deleteActiveProcesses(anyString(), anyString());

        GenericJobOutcome outcome = GenericJobOutcome.builder()
            .taskId(null)
            .processInstanceId(camundaTask.getId())
            .successful(true)
            .jobType(TASK_CLEAN_UP.name())
            .build();

        GenericJobReport expectedReport = new GenericJobReport(1, singletonList(outcome));
        assertEquals(expectedReport, actualReport);

        await().atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> assertThat(cleanUpJobService.deleteActiveProcesses(tasks, SOME_SERVICE_TOKEN))
                .isNotNull());

    }

    @Test
    void should_not_delete_active_tasks_when_environment_apart_from_aat() {
        lenient().when(cleanUpJobConfig.getEnvironment()).thenReturn("local");

        cleanUpJobService = new CleanUpJobService(
            camundaClient,
            cleanUpJobConfig,
            new ObjectMapper()
        );

        HistoricCamundaTask camundaTask = new HistoricCamundaTask(
            "ac365ec0-4220-412e-bd0c-4cc56e71f64e",
            null,
            null,
            null
        );

        List<HistoricCamundaTask> tasks = singletonList(camundaTask);

        GenericJobReport actualReport = cleanUpJobService.deleteActiveProcesses(tasks, SOME_SERVICE_TOKEN);

        GenericJobReport expectedReport = new GenericJobReport(0, emptyList());
        assertEquals(expectedReport, actualReport);

    }

    @Test
    void should_log_exception_when_an_error_occurred_in_delete_active_tasks(CapturedOutput output) {

        GenericJobReport expectedReport = new GenericJobReport(0, emptyList());
        List<HistoricCamundaTask> tasks = null;

        GenericJobReport actualReport = cleanUpJobService.deleteActiveProcesses(tasks, SOME_SERVICE_TOKEN);

        assertEquals(expectedReport, actualReport);
        assertThat(output.getOut().contains("There was no active task(s) to delete."));
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
            .jobType(TASK_CLEAN_UP.name())
            .build();

        GenericJobReport expectedReport = new GenericJobReport(1, singletonList(outcome));

        when(camundaClient.getActiveProcessCount(any()))
            .thenReturn(new CamundaTaskCount(1L));

        doThrow(FeignException.class)
            .when(camundaClient)
            .deleteActiveProcesses(
                eq(SOME_SERVICE_TOKEN),
                any()
            );


        GenericJobReport actualReport = cleanUpJobService.deleteActiveProcesses(tasks, SOME_SERVICE_TOKEN);

        assertEquals(expectedReport, actualReport);
        assertThat(output.getOut().contains("An error occurred when deleting history tasks :"));
    }

    @ParameterizedTest(name = "jobName: {0} expected: {1}")
    @CsvSource({
        "prod, false",
        "PROD, false",
        "local, true",
        "aat, true",
        "demo, true"
    })
    void should_return_a_boolean_according_to_environment(String environment, boolean expectedIsAllowedEnvironment) {

        when(cleanUpJobConfig.getEnvironment())
            .thenReturn(environment);

        boolean actualIsAllowedEnvironment = cleanUpJobService.isAllowedEnvironment();

        assertEquals(expectedIsAllowedEnvironment, actualIsAllowedEnvironment);

    }

    @Test
    void should_log_a_message_when_environment_is_allowed(CapturedOutput output) {

        String environment = "local";
        String enabledMessage = String.format("%s is enabled for this environment: %s",
            TASK_CLEAN_UP.name(), environment);


        when(cleanUpJobConfig.getEnvironment())
            .thenReturn(environment);

        boolean isAllowedEnvironment = cleanUpJobService.isAllowedEnvironment();

        assertTrue(isAllowedEnvironment);

        assertThat(output.getOut().contains(enabledMessage));

    }

    @Test
    void should_log_a_message_when_environment_is_not_allowed(CapturedOutput output) {

        String environment = "prod";
        String enabledMessage = String.format("%s is not enabled for this environment: %s",
            TASK_CLEAN_UP.name(), environment);


        when(cleanUpJobConfig.getEnvironment())
            .thenReturn(environment);

        boolean isAllowedEnvironment = cleanUpJobService.isAllowedEnvironment();

        assertFalse(isAllowedEnvironment);

        assertThat(output.getOut().contains(enabledMessage));

    }

    @Test
    void should_log_not_allowed_to_clean_task_when_environment_apart_from_aat(CapturedOutput output) {
        when(cleanUpJobConfig.getEnvironment())
            .thenReturn("demo");

        GenericJobReport expectedReport = new GenericJobReport(0, emptyList());
        List<HistoricCamundaTask> tasks = null;

        GenericJobReport actualReport = cleanUpJobService.deleteActiveProcesses(tasks, SOME_SERVICE_TOKEN);

        assertEquals(expectedReport, actualReport);

        assertThat(output.getOut().contains(
            String.format("%s clean active task is not enabled for this environment: %s",
                TASK_CLEAN_UP.name(), cleanUpJobConfig.getEnvironment())
        ));
    }


    private void assertQuery() throws JSONException {
        JSONObject query = new JSONObject(actualQueryParametersCaptor.getValue());

        String startedBefore = query.getString("startedBefore");

        assertDoesNotThrow(() -> ZonedDateTime.parse(startedBefore, cleanUpJobService.formatter));

        JSONAssert.assertEquals(
            getExpectedQueryParameters(startedBefore),
            actualQueryParametersCaptor.getValue(),
            JSONCompareMode.LENIENT
        );

    }

    @NotNull
    private String getExpectedQueryParameters(String startedBefore) {

        return "{\n"
               + " \"startedBefore\": \"" + startedBefore + "\",\n"
               + "  \"orQueries\": [\n"
               + "    {\n"
               + "      \"processVariables\": [\n"
               + "        {\n"
               + "          \"active\": true\n"
               + "        }\n"
               + "      ]\n"
               + "    },\n"
               + "    {\n"
               + "      \"processVariables\": [\n"
               + "        {\n"
               + "          \"completed\": true\n"
               + "        }\n"
               + "      ]\n"
               + "    },\n"
               + "    {\n"
               + "      \"processVariables\": [\n"
               + "        {\n"
               + "          \"suspended\": true\n"
               + "        }\n"
               + "      ]\n"
               + "    },\n"
               + "    {\n"
               + "      \"processVariables\": [\n"
               + "        {\n"
               + "          \"externallyTerminated\": true\n"
               + "        }\n"
               + "      ]\n"
               + "    },\n"
               + "    {\n"
               + "      \"processVariables\": [\n"
               + "        {\n"
               + "          \"internallyTerminated\": true\n"
               + "        }\n"
               + "      ]\n"
               + "    }\n"
               + "  ],\n"
               + "  \"sorting\": [\n"
               + "    {\n"
               + "      \"sortBy\": \"startTime\",\n"
               + "      \"sortOrder\": \"asc\"\n"
               + "    }\n"
               + "  ]\n"
               + "}";

    }

}
