package uk.gov.hmcts.reform.wataskmonitor.services.jobs.configuration;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskConfigurationClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurationJobTestService {

    public static final String SERVICE_TOKEN = "some service token";
    @Mock
    private CamundaClient camundaClient;
    @Mock
    private TaskConfigurationClient taskConfigurationClient;

    @InjectMocks
    private ConfigurationJobService configurationJobService;

    @Captor
    private ArgumentCaptor<String> actualQueryParametersCaptor;
    @Captor
    private ArgumentCaptor<String> taskIdCaptor;

    private final CamundaTask task1 = new CamundaTask(
        "some id",
        "task name 1",
        "2151a580-c3c3-11eb-8b76-d26a7287fec2"
    );

    private final CamundaTask task2 = new CamundaTask(
        "some other id",
        "task name 2",
        "2151a580-c3c3-11eb-8b76-d26a7287f000"
    );

    private final List<CamundaTask> camundaTasks = List.of(task1, task2);


    @Test
    void givenGetTasksCamundaRequestShouldRetrieveUserTasksAndNotDelayedTasks() throws JSONException {
        when(camundaClient.getTasks(
            eq(SERVICE_TOKEN),
            eq("0"),
            eq("1000"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(camundaTasks);

        List<CamundaTask> actualCamundaTasks = configurationJobService.getUnConfiguredTasks(SERVICE_TOKEN);

        assertQueryTargetsUserTasksAndNotDelayedTasks("{taskDefinitionKey: processTask}");
        assertQueryTargetsUserTasksAndNotDelayedTasks(getExpectedQueryParameters());
        assertThat(actualCamundaTasks).isEqualTo(camundaTasks);
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
               + "          \"name\": \"taskState\",\n"
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

    @Test
    void givenUnConfiguredTaskThatCanBeConfiguredShouldConfigureThemSuccessfully() {
        when(taskConfigurationClient.configureTask(
            eq(SERVICE_TOKEN),
            taskIdCaptor.capture()
        )).thenReturn("OK");

        configurationJobService.configureTasks(camundaTasks, SERVICE_TOKEN);

        assertThat(taskIdCaptor.getAllValues()).isEqualTo(List.of(task1.getId(), task2.getId()));
        verify(taskConfigurationClient, times(camundaTasks.size()))
            .configureTask(eq(SERVICE_TOKEN), anyString());
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void givenUnConfiguredTaskThatCanNotBeConfiguredShouldCatchException() {
        when(taskConfigurationClient.configureTask(any(), any())).thenThrow(new RuntimeException());
        Assertions.assertDoesNotThrow(() -> configurationJobService.configureTasks(camundaTasks, SERVICE_TOKEN));
    }

    @Test
    void givenThereAreNoTasksToConfigureShouldNotRunConfigureTaskLogic() {
        configurationJobService.configureTasks(Collections.emptyList(), SERVICE_TOKEN);

        verifyNoInteractions(taskConfigurationClient);
    }

}
