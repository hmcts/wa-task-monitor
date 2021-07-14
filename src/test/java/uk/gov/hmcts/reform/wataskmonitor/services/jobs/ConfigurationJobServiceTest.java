package uk.gov.hmcts.reform.wataskmonitor.services.jobs;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
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
import uk.gov.hmcts.reform.wataskmonitor.models.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.configuration.ConfigurationJobService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurationJobServiceTest {

    public static final String SERVICE_TOKEN = "some service token";
    @Mock
    private CamundaClient camundaClient;

    @InjectMocks
    private ConfigurationJobService configurationJobService;

    @Captor
    private ArgumentCaptor<String> actualQueryParametersCaptor;

    @Test
    void givenGetTasksCamundaRequestShouldRetrieveUserTasksAndNotDelayedTasks() throws JSONException {
        List<CamundaTask> expectedCamundaTasks = List.of(
            new CamundaTask("1", "task name", "2151a580-c3c3-11eb-8b76-d26a7287fec2"),
            new CamundaTask("2", "task name", "2151a580-c3c3-11eb-8b76-d26a7287f000")
        );
        when(camundaClient.getTasks(
            eq(SERVICE_TOKEN),
            eq("0"),
            eq("1000"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(expectedCamundaTasks);

        List<CamundaTask> actualCamundaTasks = configurationJobService.getUnConfiguredTasks(SERVICE_TOKEN);

        assertQueryTargetsUserTasksAndNotDelayedTasks("{taskDefinitionKey: processTask}");
        assertQueryTargetsUserTasksAndNotDelayedTasks(getExpectedQueryParameters());
        assertThat(actualCamundaTasks).isEqualTo(expectedCamundaTasks);
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

}
