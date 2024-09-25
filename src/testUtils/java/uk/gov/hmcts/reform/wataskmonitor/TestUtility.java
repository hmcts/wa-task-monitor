package uk.gov.hmcts.reform.wataskmonitor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

public final class TestUtility {

    private TestUtility() {
        //Utility classes should not have a public or default constructor.
    }

    public static String asJsonString(Object object) {
        try {
            return new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getExpectedRequestForUnconfiguredTasks() {
        return """
            {
              "orQueries": [
                {
                  "taskVariables": [
                    {
                      "name": "taskState",
                      "operator": "eq",
                      "value": "unconfigured"
                    }
                  ]
                }
              ],
              "taskDefinitionKey": "processTask",
              "processDefinitionKey": "wa-task-initiation-ia-asylum"
            }""";
    }

    public static String getExpectedRequestForHistoricTasksPendingTermination() {
        return """
              {
              "taskVariables": [
                {
                  "name": "cftTaskState",
                  "operator": "eq",
                  "value": "pendingTermination"
                }
              ],
              "processDefinitionKey": "wa-task-initiation-ia-asylum"
            }""";
    }
}
