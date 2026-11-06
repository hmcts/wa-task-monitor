package uk.gov.hmcts.reform.wataskmonitor.utils;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.reform.wataskmonitor.exceptions.ObjectMapperUtilityFailure;

public final class ObjectMapperUtility {

    private ObjectMapperUtility() {
        // utility class should not have a public or default constructor
    }

    public static <T> T stringToObject(String string, Class<T> valueType) {
        JsonMapper objectMapper = JsonMapper.builder().configureForJackson2().build();
        try {
            return objectMapper.readValue(string, valueType);
        } catch (JacksonException e) {
            throw new ObjectMapperUtilityFailure(
                String.format("Error deserializing object[%s] from string[%s]", valueType.toString(), string),
                e
            );
        }

    }

}
