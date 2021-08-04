package uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskManagementClient;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class InitiationJobServiceTest extends UnitBaseTest {

    @Mock
    private CamundaClient camundaClient;
    @Mock
    private TaskManagementClient taskManagementClient;
    private InitiationTaskAttributesMapper initiationTaskAttributesMapper;
    private InitiationJobService initiationJobService;
    @Captor
    private ArgumentCaptor<String> actualQueryParametersCaptor;

    @BeforeEach
    void setUp() {
        initiationTaskAttributesMapper = new InitiationTaskAttributesMapper(new ObjectMapper());
        initiationJobService = new InitiationJobService(
            camundaClient,
            taskManagementClient,
            initiationTaskAttributesMapper
        );
    }

    @Test
    void should_return_active_tasks_and_not_delayed_tasks() throws JSONException {
        List<CamundaTask> tasks = InitiationHelpers.getMockedTasks();
        when(camundaClient.getTasks(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("1000"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(tasks);

        List<CamundaTask> actualCamundaTasks = initiationJobService.getUnConfiguredTasks(SOME_SERVICE_TOKEN);

        assertQueryTargetsUserTasksAndNotDelayedTasks("{taskDefinitionKey: processTask}");
        assertQueryTargetsUserTasksAndNotDelayedTasks(getExpectedQueryParameters());
        assertThat(actualCamundaTasks).isEqualTo(tasks);
    }

    @Test
    void when_no_tasks_should_generate_report() {

        GenericJobReport actual = initiationJobService.initiateTasks(emptyList(), SOME_SERVICE_TOKEN);

        GenericJobReport expectation = new GenericJobReport(0, emptyList());
        assertEquals(expectation, actual);
    }

    @Test
    void should_succeed_and_initiate_tasks() {
        ZonedDateTime createdDate = ZonedDateTime.now();
        ZonedDateTime dueDate = ZonedDateTime.now().plusDays(1);
        CamundaTask camundaTask = InitiationHelpers.createMockedCamundaTask(
            createdDate,
            dueDate
        );
        List<CamundaTask> tasks = singletonList(camundaTask);

        Map<String, CamundaVariable> mockedVariables = InitiationHelpers.createMockCamundaVariables();
        when(camundaClient.getTask(
            SOME_SERVICE_TOKEN,
            camundaTask.getId()
        )).thenReturn(camundaTask);

        when(camundaClient.getVariables(
            SOME_SERVICE_TOKEN,
            camundaTask.getId()
        )).thenReturn(mockedVariables);

        GenericJobReport actual = initiationJobService.initiateTasks(tasks, SOME_SERVICE_TOKEN);

        GenericJobOutcome outcome = GenericJobOutcome.builder()
            .taskId(camundaTask.getId())
            .processInstanceId(camundaTask.getProcessInstanceId())
            .success(true)
            .jobType("Task Initiation")
            .build();

        GenericJobReport expectation = new GenericJobReport(1, singletonList(outcome));
        assertEquals(expectation, actual);
    }

    private void assertQueryTargetsUserTasksAndNotDelayedTasks(String expected) throws JSONException {
        JSONAssert.assertEquals(
            expected,
            actualQueryParametersCaptor.getValue(),
            JSONCompareMode.LENIENT
        );
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
               + "  \"processDefinitionKey\": \"wa-task-initiation-ia-asylum\"\n"
               + "}\n";
    }

}
