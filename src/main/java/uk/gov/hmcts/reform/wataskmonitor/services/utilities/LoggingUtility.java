package uk.gov.hmcts.reform.wataskmonitor.services.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.wataskmonitor.exceptions.PrettyPrintFailure;

public final class LoggingUtility {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String logPrettyPrint(String str) {
        try {
            Object json = mapper.readValue(str, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (JsonProcessingException e) {
            throw new PrettyPrintFailure("Error logging pretty print: " + str, e);
        }
    }

    public static String logPrettyPrint(Object obj) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new PrettyPrintFailure("Error logging pretty print: " + obj, e);
        }
    }

    private LoggingUtility() {
        // utility class should not have a public or default constructor
    }

}
