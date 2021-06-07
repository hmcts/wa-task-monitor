package uk.gov.hmcts.reform.wataskmonitor.services;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.models.Task;

import java.util.List;
import java.util.Map;

@Component
public class CamundaService {

    private final CamundaClient camundaClient;

    public CamundaService(CamundaClient camundaClient) {
        this.camundaClient = camundaClient;
    }

    String queryParameters = "{\n" +
        "    \"orQueries\": [\n" +
        "        {\n" +
        "            \"taskVariables\": [\n" +
        "                {\n" +
        "                    \"name\": \"taskState\",\n" +
        "                    \"operator\": \"eq\",\n" +
        "                    \"value\": \"unconfigured\"\n" +
        "                }\n" +
        "            ]\n" +
        "        }\n" +
        "    ],\n" +
        "    \"createdBefore\": \"2021-06-02T16:18:00.808+0000\",\n" +
        "    \"taskDefinitionKey\": \"processTask\",\n" +
        "    \"processDefinitionKey\": \"wa-task-initiation-ia-asylum\"\n" +
        "}";

    public List<Task> getTasks() {
        return camundaClient.getTasks("s2s token", null, null, Map.of());
    }

}
