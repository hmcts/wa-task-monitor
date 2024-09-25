package uk.gov.hmcts.reform.wataskmonitor.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CreateTaskJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.exceptions.LoggingUtilityFailure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class LoggingUtilityTest {

    @Test
    void logPrettyPrintStringArgument() {
        String input = "{\"job_details\" : {\"name\" : \"CONFIGURATION\"}}";

        @SuppressWarnings("PMD.LawOfDemeter")
        String output = LoggingUtility.logPrettyPrint(input);

        String expectedOutput = """
            {
              "job_details" : {
                "name" : "CONFIGURATION"
              }
            }""";

        assertEquals(expectedOutput, output, "output does not match expected output");
        assertNotEquals(output, input, "output can't be equal to input");
    }

    @Test
    void logPrettyPrintStringArgumentShouldThrowException() {
        assertThrows(LoggingUtilityFailure.class, () -> LoggingUtility.logPrettyPrint("invalid input"));
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

        String expectedOutput = """
            {
              "caseId" : "someCaseId",
              "taskId" : "some task id",
              "processInstanceId" : "some process instance id",
              "created" : true
            }""";

        assertEquals(expectedOutput, output, "output does not match expected output");
    }

    @Test
    void logPrettyPrintObjectArgumentArgumentShouldThrowException() {
        assertThrows(LoggingUtilityFailure.class, () -> LoggingUtility.logPrettyPrint(new Object()));
    }

}
