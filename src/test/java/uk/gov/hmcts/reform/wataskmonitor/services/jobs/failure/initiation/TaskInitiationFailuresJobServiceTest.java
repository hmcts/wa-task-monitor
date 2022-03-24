package uk.gov.hmcts.reform.wataskmonitor.services.jobs.failure.initiation;

import feign.FeignException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.config.job.InitiationJobConfig;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaVariable;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation.helpers.InitiationHelpers;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(OutputCaptureExtension.class)
class TaskInitiationFailuresJobServiceTest extends UnitBaseTest {

    @Mock
    private CamundaClient camundaClient;
    @Mock
    private InitiationJobConfig initiationJobConfig;

    private TaskInitiationFailuresJobService taskInitiationFailuresJobService;

    @Captor
    private ArgumentCaptor<String> actualQueryParametersCaptor;

    @BeforeEach
    void setUp() {
        taskInitiationFailuresJobService = new TaskInitiationFailuresJobService(
            camundaClient,
            initiationJobConfig
        );
        lenient().when(initiationJobConfig.getCamundaMaxResults()).thenReturn("100");
        lenient().when(initiationJobConfig.isCamundaTimeLimitFlag()).thenReturn(true);
        lenient().when(initiationJobConfig.getCamundaTimeLimit()).thenReturn(120L);
    }

    @Test
    void should_return_active_tasks_and_not_delayed_tasks(CapturedOutput output) throws JSONException {
        ZonedDateTime createdDate = ZonedDateTime.now();
        ZonedDateTime dueDate = ZonedDateTime.now().plusDays(1);
        CamundaTask camundaTask = InitiationHelpers.createMockedCamundaTask(
            createdDate,
            dueDate
        );
        List<CamundaTask> camundaTasks = singletonList(camundaTask);

        when(camundaClient.getTasks(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(camundaTasks);

        Map<String, CamundaVariable> mockedVariables = InitiationHelpers.createMockCamundaVariables();

        lenient().when(camundaClient.getVariables(
            SOME_SERVICE_TOKEN,
            camundaTask.getId()
        )).thenReturn(mockedVariables);

        GenericJobReport genericJobReport = taskInitiationFailuresJobService.getUnInitiatedTasks(SOME_SERVICE_TOKEN);

        assertQueryTargetsUserTasksAndNotDelayedTasks();
        assertQuery(true);
        assertThat(genericJobReport.getTotalTasks()).isEqualTo(camundaTasks.size());
        assertThat(genericJobReport.getOutcomeList().size()).isEqualTo(camundaTasks.size());
        assertTrue(genericJobReport.getOutcomeList().get(0).isSuccessful());
        assertThat(output).contains("TASK_INITIATION_FAILURES There are some uninitiated tasks");
        
    }

    @Test
    void should_return_empty_list_when_camundaTasks_is_empty(CapturedOutput output) throws JSONException {

        when(camundaClient.getTasks(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(emptyList());

        GenericJobReport genericJobReport = taskInitiationFailuresJobService.getUnInitiatedTasks(SOME_SERVICE_TOKEN);

        assertQueryTargetsUserTasksAndNotDelayedTasks();
        assertQuery(true);
        assertThat(genericJobReport.getTotalTasks()).isEqualTo(0);
        assertThat(genericJobReport.getOutcomeList().size()).isEqualTo(0);
        assertThat(output).contains("TASK_INITIATION_FAILURES There was no task");
    }

    @Test
    void should_return_isSuccessful_false_when_an_exception_thrown() throws JSONException {
        ZonedDateTime createdDate = ZonedDateTime.now();
        ZonedDateTime dueDate = ZonedDateTime.now().plusDays(1);
        CamundaTask camundaTask = InitiationHelpers.createMockedCamundaTask(
            createdDate,
            dueDate
        );
        List<CamundaTask> camundaTasks = singletonList(camundaTask);

        when(camundaClient.getTasks(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(camundaTasks);

        when(camundaClient.getVariables(
            SOME_SERVICE_TOKEN,
            camundaTask.getId()
        )).thenThrow(FeignException.class);

        GenericJobReport genericJobReport = taskInitiationFailuresJobService.getUnInitiatedTasks(SOME_SERVICE_TOKEN);

        assertQueryTargetsUserTasksAndNotDelayedTasks();
        assertQuery(true);
        assertThat(genericJobReport.getTotalTasks()).isEqualTo(camundaTasks.size());
        assertThat(genericJobReport.getOutcomeList().size()).isEqualTo(camundaTasks.size());
        assertFalse(genericJobReport.getOutcomeList().get(0).isSuccessful());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void should_createdBefore_exists_or_not_in_query_according_to_initiation_flag(boolean timeFlag)
        throws JSONException {

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

        Map<String, CamundaVariable> mockedVariables = InitiationHelpers.createMockCamundaVariables();

        lenient().when(camundaClient.getVariables(
            SOME_SERVICE_TOKEN,
            camundaTask.getId()
        )).thenReturn(mockedVariables);

        taskInitiationFailuresJobService = new TaskInitiationFailuresJobService(
            camundaClient,
            initiationJobConfig
        );
        lenient().when(initiationJobConfig.isCamundaTimeLimitFlag()).thenReturn(timeFlag);

        taskInitiationFailuresJobService.getUnInitiatedTasks(SOME_SERVICE_TOKEN);

        assertQuery(timeFlag);

    }

    private void assertQuery(boolean timeFlag) throws JSONException {
        JSONObject query = new JSONObject(actualQueryParametersCaptor.getValue());
        if (timeFlag) {
            String createdBefore = query.getString("createdBefore");
            JSONAssert.assertEquals(
                getExpectedQueryParameters(createdBefore),
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
    private String getExpectedQueryParameters(String createdBefore) {
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
               + " \"createdBefore\": \"" + createdBefore + "\",\n"
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
