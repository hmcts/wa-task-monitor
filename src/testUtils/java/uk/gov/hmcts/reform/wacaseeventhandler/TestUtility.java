package uk.gov.hmcts.reform.wacaseeventhandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class TestUtility {

    private TestUtility() {
        //Utility classes should not have a public or default constructor.
    }

    public static String asJsonString(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getExpectedCamundaQueryParameters() {
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
