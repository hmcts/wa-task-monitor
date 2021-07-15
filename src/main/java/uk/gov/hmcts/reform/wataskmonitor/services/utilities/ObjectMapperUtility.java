package uk.gov.hmcts.reform.wataskmonitor.services.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.wataskmonitor.exceptions.ObjectMapperUtilityFailure;

public class ObjectMapperUtility {

    private ObjectMapperUtility() {
        // utility class should not have a public or default constructor
    }

    public static <T> T stringToObject(String string, Class<T> valueType) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(string, valueType);
        } catch (JsonProcessingException e) {
            throw new ObjectMapperUtilityFailure(
                String.format("Error deserializing object[%s] from string[%s]", valueType.toString(), string),
                e);
        }

    }

}
