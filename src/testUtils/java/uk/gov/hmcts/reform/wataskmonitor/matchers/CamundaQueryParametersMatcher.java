package uk.gov.hmcts.reform.wataskmonitor.matchers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.mockito.ArgumentMatcher;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class CamundaQueryParametersMatcher implements ArgumentMatcher<String> {
    public static final String CAMUNDA_DATE_REQUEST_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private final String expectedCamundaQueryParameters;

    public CamundaQueryParametersMatcher(String expectedCamundaQueryParameters) {
        this.expectedCamundaQueryParameters = expectedCamundaQueryParameters;
    }

    @SneakyThrows
    @Override
    public boolean matches(String actualCamundaQueryParameters) {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode expected = mapper.readTree(expectedCamundaQueryParameters);
        log.debug(expected.toPrettyString());

        JsonNode actual = mapper.readTree(actualCamundaQueryParameters);
        log.debug(actual.toPrettyString());

        return actual.path("orQueries").equals(expected.path("orQueries"))
               && actual.path("taskDefinitionKey").equals(expected.path("taskDefinitionKey"))
               && actual.path("processDefinitionKey").equals(expected.path("processDefinitionKey"))
               && isCreatedBeforeValid(String.valueOf(actual.get("createdBefore").asText()));
    }

    private boolean isCreatedBeforeValid(String actualCreatedBefore) {
        return StringUtils.isNotBlank(LocalDateTime.parse(
            actualCreatedBefore,
            DateTimeFormatter.ofPattern(CAMUNDA_DATE_REQUEST_PATTERN)
        ).toString());
    }
}
