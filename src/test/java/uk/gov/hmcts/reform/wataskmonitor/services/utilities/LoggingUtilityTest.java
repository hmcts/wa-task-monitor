package uk.gov.hmcts.reform.wataskmonitor.services.utilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


class LoggingUtilityTest {

    @Test
    void logPrettyPrint() {
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

}