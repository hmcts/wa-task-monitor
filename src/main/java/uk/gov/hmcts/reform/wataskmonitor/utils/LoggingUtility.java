package uk.gov.hmcts.reform.wataskmonitor.utils;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.reform.wataskmonitor.exceptions.LoggingUtilityFailure;

public final class LoggingUtility {

    private static final JsonMapper MAPPER = JsonMapper.builder().configureForJackson2().build();

    public static String logPrettyPrint(String str) {
        try {
            Object json = MAPPER.readValue(str, Object.class);
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (JacksonException e) {
            throw new LoggingUtilityFailure("Error logging pretty print: " + str, e);
        }
    }

    public static String logPrettyPrint(Object obj) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JacksonException e) {
            throw new LoggingUtilityFailure("Error logging pretty print: " + obj, e);
        }
    }

    private LoggingUtility() {
        // utility class should not have a public or default constructor
    }

}
