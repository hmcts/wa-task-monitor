package uk.gov.hmcts.reform.wataskmonitor.services;

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
import uk.gov.hmcts.reform.wataskmonitor.models.CamundaTask;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CamundaServiceTest {

    @Mock
    private CamundaClient camundaClient;

    @InjectMocks
    private CamundaService camundaService;

    @Captor
    private ArgumentCaptor<String> actualQueryParametersCaptor;

    @Test
    void givenGetTasksCamundaRequestShouldRetrieveTasks() throws JSONException {
        List<CamundaTask> expectedCamundaTasks = List.of(new CamundaTask("1"), new CamundaTask("2"));
        when(camundaClient.getTasks(
            anyString(),
            eq("0"),
            eq("1000"),
            actualQueryParametersCaptor.capture()
        )).thenReturn(expectedCamundaTasks);

        List<CamundaTask> actualCamundaTasks = camundaService.getUnConfiguredTasks();

        JSONAssert.assertEquals(
            getExpectedQueryParameters(),
            actualQueryParametersCaptor.getValue(),
            JSONCompareMode.LENIENT
        );

        assertThat(actualCamundaTasks).isEqualTo(expectedCamundaTasks);
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
