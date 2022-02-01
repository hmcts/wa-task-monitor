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

    private final CamundaTask task1 = new CamundaTask(
        "some id",
        "task name 1",
        "2151a580-c3c3-11eb-8b76-d26a7287fec2",
        null,
        null,
        null,
        null,
        null,
        null
    );
    private final CamundaTask task2 = new CamundaTask(
        "some other id",
        "task name 2",
        "2151a580-c3c3-11eb-8b76-d26a7287f000",
        null,
        null,
        null,
        null,
        null,
        null
    );
    private final List<CamundaTask> camundaTasks = List.of(task1, task2);
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
        configurationJobService = new ConfigurationJobService(
            camundaClient,
            taskConfigurationClient,
            configurationJobConfig,
            true,
            60
        );
    }

    @Test
    void givenGetTasksCamundaRequestShouldRetrieveUserTasksAndNotDelayedTasks() throws JSONException {
        when(configurationJobConfig.getCamundaMaxResults()).thenReturn("10");

        when(camundaClient.getTasks(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("10"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(camundaTasks);

        List<CamundaTask> actualCamundaTasks = configurationJobService.getUnConfiguredTasks(SOME_SERVICE_TOKEN);

        assertQueryTargetsUserTasksAndNotDelayedTasks("{taskDefinitionKey: processTask}");
        assertQuery();
        assertThat(actualCamundaTasks).isEqualTo(camundaTasks);
    }

    @Test
    void givenUnConfiguredTaskThatCanBeConfiguredShouldConfigureThemSuccessfully() {
        when(taskConfigurationClient.configureTask(
            eq(SOME_SERVICE_TOKEN),
            taskIdCaptor.capture()
        )).thenReturn("OK");

        configurationJobService.configureTasks(camundaTasks, SOME_SERVICE_TOKEN);

        assertThat(taskIdCaptor.getAllValues()).isEqualTo(List.of(task1.getId(), task2.getId()));
        verify(taskConfigurationClient, times(camundaTasks.size()))
            .configureTask(eq(SOME_SERVICE_TOKEN), anyString());
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void givenUnConfiguredTaskThatCanNotBeConfiguredShouldCatchException() {
        when(taskConfigurationClient.configureTask(any(), any())).thenThrow(new RuntimeException());
        Assertions.assertDoesNotThrow(() -> configurationJobService.configureTasks(camundaTasks, SOME_SERVICE_TOKEN));
    }

    @Test
    void givenThereAreNoTasksToConfigureShouldNotRunConfigureTaskLogic() {
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
    void should_configure_tasks_according_to_time_flag(boolean configurationTimeLimitFlag) throws JSONException {

        ZonedDateTime createdDate = ZonedDateTime.now().minusMinutes(100);
        ZonedDateTime dueDate = ZonedDateTime.now().plusDays(1);
        CamundaTask camundaTask = createMockedCamundaTask(createdDate, dueDate);

        List<CamundaTask> expectedCamundaTasks = List.of(camundaTask);

        configurationJobService = new ConfigurationJobService(
            camundaClient,
            taskConfigurationClient,
            configurationJobConfig,
            configurationTimeLimitFlag,
            60
        );

        configurationJobService.configureTasks(expectedCamundaTasks, SOME_SERVICE_TOKEN);
        assertEquals(configurationTimeLimitFlag, configurationJobService.isConfigurationTimeLimitFlag());
        assertEquals(60, configurationJobService.getConfigurationTimeLimit());
        verify(taskConfigurationClient, times(1))
            .configureTask(eq(SOME_SERVICE_TOKEN), any());

    }

    private void assertQuery() throws JSONException {
        JSONObject query = new JSONObject(actualQueryParametersCaptor.getValue());
        String createdAfter = query.getString("createdAfter");
        JSONAssert.assertEquals(
            getExpectedQueryParameters(createdAfter),
            actualQueryParametersCaptor.getValue(),
            JSONCompareMode.LENIENT
        );
    }

    private void assertQueryTargetsUserTasksAndNotDelayedTasks(String expected) throws JSONException {
        JSONAssert.assertEquals(
            expected,
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
