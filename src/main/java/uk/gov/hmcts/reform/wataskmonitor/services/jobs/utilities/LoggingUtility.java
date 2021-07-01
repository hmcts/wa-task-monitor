package uk.gov.hmcts.reform.wataskmonitor.services.jobs.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.wataskmonitor.exceptions.PrettyPrintFailure;

import java.util.function.Function;

public final class LoggingUtility {
    private LoggingUtility() {
        // utility class should not have a public or default constructor
    }

    public static Function<String, String> logPrettyPrint = (response) ->
    {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Object json = objectMapper.readValue(response, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (JsonProcessingException e) {
            throw new PrettyPrintFailure("Error logging pretty print: " + response, e);
        }
    };

}
