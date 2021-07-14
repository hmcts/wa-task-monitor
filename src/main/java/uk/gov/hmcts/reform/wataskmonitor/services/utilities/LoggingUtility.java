package uk.gov.hmcts.reform.wataskmonitor.services.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.wataskmonitor.exceptions.PrettyPrintFailure;

public final class LoggingUtility {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String logPrettyPrint(String str) {
        try {
            Object json = MAPPER.readValue(str, Object.class);
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (JsonProcessingException e) {
            throw new PrettyPrintFailure("Error logging pretty print: " + str, e);
        }
    }

    public static String logPrettyPrint(Object obj) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new PrettyPrintFailure("Error logging pretty print: " + obj, e);
        }
    }

    private LoggingUtility() {
        // utility class should not have a public or default constructor
    }

}
