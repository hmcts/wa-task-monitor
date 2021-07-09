package uk.gov.hmcts.reform.wataskmonitor.services.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.wataskmonitor.exceptions.PrettyPrintFailure;

import java.util.function.Function;

public final class LoggingUtility {

    public static Function<String, String> logPrettyPrint = (str) -> {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Object json = objectMapper.readValue(str, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (JsonProcessingException e) {
            throw new PrettyPrintFailure("Error logging pretty print: " + str, e);
        }
    };

    private LoggingUtility() {
        // utility class should not have a public or default constructor
    }

}
