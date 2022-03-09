package uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.wataskmonitor.config.job.InitiationJobConfig;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaVariable;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InitiationJobServiceTest extends UnitBaseTest {

    @Mock
    private CamundaClient camundaClient;
    @Mock
    private TaskManagementClient taskManagementClient;
    private InitiationTaskAttributesMapper initiationTaskAttributesMapper;
    @Mock
    private InitiationJobConfig initiationJobConfig;
    private InitiationJobService initiationJobService;
    @Captor
    private ArgumentCaptor<String> actualQueryParametersCaptor;

    @BeforeEach
    void setUp() {
        initiationTaskAttributesMapper =
            new InitiationTaskAttributesMapper(new ObjectMapper());
        initiationJobService = new InitiationJobService(
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
        List<CamundaTask> tasks = InitiationHelpers.getMockedTasks();
        when(camundaClient.getTasks(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(tasks);

        List<CamundaTask> actualCamundaTasks = initiationJobService.getUnConfiguredTasks(SOME_SERVICE_TOKEN);

        assertQueryTargetsUserTasksAndNotDelayedTasks();
        assertQuery(true);
        assertThat(actualCamundaTasks).isEqualTo(tasks);
    }

    @Test
    void when_no_tasks_should_generate_report() {

        GenericJobReport actual = initiationJobService.initiateTasks(emptyList(), SOME_SERVICE_TOKEN);

        GenericJobReport expectation = new GenericJobReport(0, emptyList());
        assertEquals(expectation, actual);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void should_succeed_and_initiate_tasks(boolean timeFlag) {
        initiationJobService = new InitiationJobService(
            camundaClient,
            taskManagementClient,
            initiationTaskAttributesMapper,
            initiationJobConfig
        );

        lenient().when(initiationJobConfig.isCamundaTimeLimitFlag()).thenReturn(timeFlag);

        ZonedDateTime createdDate = ZonedDateTime.now();
        ZonedDateTime dueDate = ZonedDateTime.now().plusDays(1);
        CamundaTask camundaTask = InitiationHelpers.createMockedCamundaTask(
            createdDate,
            dueDate
        );
        List<CamundaTask> tasks = singletonList(camundaTask);

        Map<String, CamundaVariable> mockedVariables = InitiationHelpers.createMockCamundaVariables();

        when(camundaClient.getVariables(
            SOME_SERVICE_TOKEN,
            camundaTask.getId()
        )).thenReturn(mockedVariables);

        GenericJobReport actual = initiationJobService.initiateTasks(tasks, SOME_SERVICE_TOKEN);

        verify(taskManagementClient, times(1))
            .initiateTask(anyString(), anyString(), any());

        GenericJobOutcome outcome = GenericJobOutcome.builder()
            .taskId(camundaTask.getId())
            .processInstanceId(camundaTask.getProcessInstanceId())
            .successful(true)
            .jobType("Task Initiation")
            .build();

        GenericJobReport expectation = new GenericJobReport(1, singletonList(outcome));
        assertEquals(expectation, actual);

        assertEquals(timeFlag, initiationJobService.isInitiationTimeLimitFlag());
        assertEquals(120, initiationJobService.getInitiationTimeLimit());

    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void should_createdAfter_exists_or_not_in_query_according_to_initiation_flag(
        boolean timeFlag) throws JSONException {

        ZonedDateTime createdDate = ZonedDateTime.now();
        ZonedDateTime dueDate = ZonedDateTime.now().plusDays(1);
        CamundaTask camundaTask = InitiationHelpers.createMockedCamundaTask(
            createdDate,
            dueDate
        );
        List<CamundaTask> camundaTasks = singletonList(camundaTask);

        when(initiationJobConfig.getCamundaMaxResults()).thenReturn("10");

        when(camundaClient.getTasks(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("10"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(camundaTasks);

        initiationJobService = new InitiationJobService(
            camundaClient,
            taskManagementClient,
            initiationTaskAttributesMapper,
            initiationJobConfig
        );

        initiationJobService.getUnConfiguredTasks(SOME_SERVICE_TOKEN);

        assertQuery(timeFlag);

    }

    @ParameterizedTest
    @CsvSource({
        "false, 100, true",
        "true, 1, false"
    })
    void should_succeed_and_initiate_tasks_according_to_migration_flag(
        boolean migrationFlag, String camundaMaxResult, boolean timeFlag) {

        initiationJobService = new InitiationJobService(
            camundaClient,
            taskManagementClient,
            initiationTaskAttributesMapper,
            initiationJobConfig
        );

        Migration migration = spy(Migration.class);
        lenient().when(initiationJobConfig.getMigration()).thenReturn(migration);
        lenient().when(migration.isMigrationFlag()).thenReturn(migrationFlag);
        lenient().when(migration.getCamundaMaxResults()).thenReturn(camundaMaxResult);

        lenient().when(initiationJobConfig.isCamundaTimeLimitFlag()).thenReturn(timeFlag);
        lenient().when(initiationJobConfig.getCamundaMaxResults()).thenReturn(camundaMaxResult);

        ZonedDateTime createdDate = ZonedDateTime.now();
        ZonedDateTime dueDate = ZonedDateTime.now().plusDays(1);
        CamundaTask camundaTask = InitiationHelpers.createMockedCamundaTask(
            createdDate,
            dueDate
        );
        List<CamundaTask> tasks = singletonList(camundaTask);

        Map<String, CamundaVariable> mockedVariables = InitiationHelpers.createMockCamundaVariables();

        when(camundaClient.getVariables(
            SOME_SERVICE_TOKEN,
            camundaTask.getId()
        )).thenReturn(mockedVariables);

        GenericJobReport actual = initiationJobService.initiateTasks(tasks, SOME_SERVICE_TOKEN);

        verify(taskManagementClient, times(1))
            .initiateTask(anyString(), anyString(), any());

        GenericJobOutcome outcome = GenericJobOutcome.builder()
            .taskId(camundaTask.getId())
            .processInstanceId(camundaTask.getProcessInstanceId())
            .successful(true)
            .jobType("Task Initiation")
            .build();

        GenericJobReport expectation = new GenericJobReport(1, singletonList(outcome));
        assertEquals(expectation, actual);

        assertEquals(timeFlag, initiationJobService.isInitiationTimeLimitFlag());
        assertEquals(120, initiationJobService.getInitiationTimeLimit());
        assertEquals(camundaMaxResult, initiationJobService.getMaxResults());

    }

    @ParameterizedTest
    @CsvSource({
        "false, true, 1, 100",
        "true, true, 1, 1"
    })
    void validate_configuration_parameters(
        boolean migrationFlag, boolean initiationTimeLimitFlag, long initiationTimeLimit, String maxResult) {

        lenient().when(initiationJobConfig.isCamundaTimeLimitFlag()).thenReturn(initiationTimeLimitFlag);
        lenient().when(initiationJobConfig.getCamundaTimeLimit()).thenReturn(initiationTimeLimit);
        lenient().when(initiationJobConfig.getCamundaMaxResults()).thenReturn(maxResult);

        Migration migration = spy(Migration.class);
        lenient().when(initiationJobConfig.getMigration()).thenReturn(migration);
        lenient().when(migration.isMigrationFlag()).thenReturn(migrationFlag);
        lenient().when(migration.isCamundaTimeLimitFlag()).thenReturn(initiationTimeLimitFlag);
        lenient().when(migration.getCamundaTimeLimit()).thenReturn(initiationTimeLimit);
        lenient().when(migration.getCamundaMaxResults()).thenReturn(maxResult);

        initiationJobService = new InitiationJobService(
            camundaClient,
            taskManagementClient,
            initiationTaskAttributesMapper,
            initiationJobConfig
        );
        
        boolean actualInitiationTimeLimitFlag = initiationJobService.isInitiationTimeLimitFlag();
        long actualInitiationTimeLimit = initiationJobService.getInitiationTimeLimit();
        String actualMaxResult = initiationJobService.getMaxResults();

        assertEquals(initiationTimeLimitFlag, actualInitiationTimeLimitFlag);
        assertEquals(initiationTimeLimit, actualInitiationTimeLimit);
        assertEquals(maxResult, actualMaxResult);

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

}
