package uk.gov.hmcts.reform.wataskmonitor.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CreateTaskJobOutcome;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


class LoggingUtilityTest {

    @Test
    void logPrettyPrintStringArgument() {
        String input = "{\"job_details\" : {\"name\" : \"CONFIGURATION\"}}";

        @SuppressWarnings("PMD.LawOfDemeter")
        String output = LoggingUtility.logPrettyPrint(input);

        String expectedOutput = "{\n"
                                + "  \"job_details\" : {\n"
                                + "    \"name\" : \"CONFIGURATION\"\n"
                                + "  }\n"
                                + "}";

        assertEquals(expectedOutput, output, "output does not match expected output");
        assertNotEquals(output, input, "output can't be equal to input");
    }

    @Test
    void logPrettyPrintObjectArgument() {
        CreateTaskJobOutcome input = CreateTaskJobOutcome.builder()
            .taskId("some task id")
            .processInstanceId("some process instance id")
            .caseId("someCaseId")
            .created(true)
            .build();

        @SuppressWarnings("PMD.LawOfDemeter")
        String output = LoggingUtility.logPrettyPrint(input);

        String expectedOutput = "{\n"
                                + "  \"caseId\" : \"someCaseId\",\n"
                                + "  \"taskId\" : \"some task id\",\n"
                                + "  \"processInstanceId\" : \"some process instance id\",\n"
                                + "  \"created\" : true\n"
                                + "}";

        assertEquals(expectedOutput, output, "output does not match expected output");
    }

}
