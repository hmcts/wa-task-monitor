package uk.gov.hmcts.reform.wataskmonitor.services.jobs.utilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


class LoggingUtilityTest {

    @Test
    void logPrettyPrint() {
        String input = "{\"job_details\" : {\"name\" : \"CONFIGURATION\"}}";

        String output = LoggingUtility.logPrettyPrint.apply(input);

        String expectedOutput = "{\n"
                                + "  \"job_details\" : {\n"
                                + "    \"name\" : \"CONFIGURATION\"\n"
                                + "  }\n"
                                + "}";

        assertEquals(expectedOutput, output);
        assertNotEquals(output, input);
    }

}