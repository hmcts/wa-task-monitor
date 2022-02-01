package uk.gov.hmcts.reform.wataskmonitor.services.jobs.configuration;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
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
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskConfigurationClient;
import uk.gov.hmcts.reform.wataskmonitor.config.job.ConfigurationJobConfig;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ConfigurationJobServiceTest extends UnitBaseTest {

    private List<CamundaTask> camundaTasks;

    @Mock
    private CamundaClient camundaClient;
    @Mock
    private TaskConfigurationClient taskConfigurationClient;
    @Mock
    private ConfigurationJobConfig configurationJobConfig;

    private ConfigurationJobService configurationJobService;

    @Captor
    private ArgumentCaptor<String> actualQueryParametersCaptor;
    @Captor
    private ArgumentCaptor<String> taskIdCaptor;

    @BeforeEach
    void setUp() {
        ZonedDateTime createdDate = ZonedDateTime.now();
        ZonedDateTime dueDate = ZonedDateTime.now().plusDays(1);
        camundaTasks = new ArrayList<>();
        CamundaTask task1 = createMockedCamundaTask(createdDate, dueDate);
        camundaTasks.add(task1);
        CamundaTask task2 = createMockedCamundaTask(createdDate, dueDate);
        camundaTasks.add(task2);

        configurationJobService = new ConfigurationJobService(
            camundaClient,
            taskConfigurationClient,
            configurationJobConfig,
            true,
            60
        );
    }

    @Test
    void should_return_user_tasks_not_delayed_tasks_when_getUnConfiguredTasks_called() throws JSONException {
        when(configurationJobConfig.getCamundaMaxResults()).thenReturn("10");

        when(camundaClient.getTasks(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("10"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(camundaTasks);

        List<CamundaTask> actualCamundaTasks = configurationJobService.getUnConfiguredTasks(SOME_SERVICE_TOKEN);

        assertQueryTargetsUserTasksAndNotDelayedTasks();
        assertQuery(true);
        assertThat(actualCamundaTasks).isEqualTo(camundaTasks);
    }

    @Test
    void given_unconfigured_task_that_can_be_configured_should_configure_them_successfully() {
        when(taskConfigurationClient.configureTask(
            eq(SOME_SERVICE_TOKEN),
            taskIdCaptor.capture()
        )).thenReturn("OK");

        configurationJobService.configureTasks(camundaTasks, SOME_SERVICE_TOKEN);

        assertThat(taskIdCaptor.getAllValues()).isEqualTo(
            camundaTasks.stream().map(CamundaTask::getId).collect(Collectors.toList())
        );
        verify(taskConfigurationClient, times(camundaTasks.size()))
            .configureTask(eq(SOME_SERVICE_TOKEN), anyString());
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void given_unconfigured_task_that_can_not_be_configured_should_catch_exception() {
        when(taskConfigurationClient.configureTask(any(), any())).thenThrow(new RuntimeException());
        Assertions.assertDoesNotThrow(() -> configurationJobService.configureTasks(camundaTasks, SOME_SERVICE_TOKEN));
    }

    @Test
    void given_there_are_no_tasks_to_configure_should_not_run_configure_task_logic() {
        configurationJobService.configureTasks(Collections.emptyList(), SOME_SERVICE_TOKEN);

        verifyNoInteractions(taskConfigurationClient);
    }

    @Test
    void should_return_values_when_camundaTasks_has_values() {
        when(taskConfigurationClient.configureTask(
            eq(SOME_SERVICE_TOKEN),
            taskIdCaptor.capture()
        )).thenReturn("OK");

        GenericJobReport genericJobReport = configurationJobService.configureTasks(camundaTasks, SOME_SERVICE_TOKEN);
        assertEquals(2, genericJobReport.getTotalTasks());
        assertEquals(2, genericJobReport.getOutcomeList().size());

        assertEquals(2, genericJobReport.getTotalNumberOfSuccesses());
        assertEquals(0, genericJobReport.getTotalNumberOfFailures());
        assertEquals(2, genericJobReport.getTotalNumberOfTasksProcessed());

    }

    @Test
    void should_return_empty_list_when_camundaTasks_empty() {
        List<CamundaTask> emptyCamundaTasks = new ArrayList<>();

        GenericJobReport genericJobReport =
            configurationJobService.configureTasks(emptyCamundaTasks, SOME_SERVICE_TOKEN);
        assertEquals(0, genericJobReport.getTotalTasks());
        assertEquals(emptyList(), genericJobReport.getOutcomeList());
        assertEquals(0, genericJobReport.getOutcomeList().size());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void should_configure_tasks_according_to_time_flag(boolean configurationTimeLimitFlag) {

        ZonedDateTime createdDate = ZonedDateTime.now().minusMinutes(100);
        ZonedDateTime dueDate = ZonedDateTime.now().plusDays(1);
        CamundaTask camundaTask = createMockedCamundaTask(createdDate, dueDate);

        camundaTasks = List.of(camundaTask);

        configurationJobService = new ConfigurationJobService(
            camundaClient,
            taskConfigurationClient,
            configurationJobConfig,
            configurationTimeLimitFlag,
            60
        );

        configurationJobService.configureTasks(camundaTasks, SOME_SERVICE_TOKEN);
        assertEquals(configurationTimeLimitFlag, configurationJobService.isConfigurationTimeLimitFlag());
        assertEquals(60, configurationJobService.getConfigurationTimeLimit());
        verify(taskConfigurationClient, times(1))
            .configureTask(eq(SOME_SERVICE_TOKEN), any());

    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void should_createdAfter_exists_or_not_in_query_according_to_configuration_flag(
        boolean configurationTimeLimitFlag) throws JSONException {
        when(configurationJobConfig.getCamundaMaxResults()).thenReturn("10");

        when(camundaClient.getTasks(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("10"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(camundaTasks);

        configurationJobService = new ConfigurationJobService(
            camundaClient,
            taskConfigurationClient,
            configurationJobConfig,
            configurationTimeLimitFlag,
            60
        );

        configurationJobService.getUnConfiguredTasks(SOME_SERVICE_TOKEN);
        assertQuery(configurationTimeLimitFlag);

    }

    private void assertQuery(boolean configurationTimeLimitFlag) throws JSONException {
        JSONObject query = new JSONObject(actualQueryParametersCaptor.getValue());
        if (configurationTimeLimitFlag) {
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
               + "          \"name\": \"taskState\",\n"
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
        return "{\n"
               + "  \"orQueries\": [\n"
               + "    {\n"
               + "      \"taskVariables\": [\n"
               + "        {\n"
               + "          \"name\": \"taskState\",\n"
               + "          \"operator\": \"eq\",\n"
               + "          \"value\": \"unconfigured\"\n"
               + "        }\n"
               + "      ]\n"
               + "    }\n"
               + "  ],\n"
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

    public static CamundaTask createMockedCamundaTask(ZonedDateTime createdDate, ZonedDateTime dueDate) {
        return new CamundaTask(
            "someCamundaTaskId",
            "someCamundaTaskName",
            "someProcessInstanceId",
            "someAssignee",
            createdDate,
            dueDate,
            "someCamundaTaskDescription",
            "someCamundaTaskOwner",
            "someCamundaTaskFormKey"
        );
    }
}
