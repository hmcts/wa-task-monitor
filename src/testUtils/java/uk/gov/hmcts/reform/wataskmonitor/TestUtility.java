package uk.gov.hmcts.reform.wataskmonitor;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

public final class TestUtility {

    private TestUtility() {
        //Utility classes should not have a public or default constructor.
    }

    public static String asJsonString(Object object) {
        try {
            return JsonMapper.builder()
                .configureForJackson2()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build()
                .writeValueAsString(object);
        } catch (JacksonException e) {
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
