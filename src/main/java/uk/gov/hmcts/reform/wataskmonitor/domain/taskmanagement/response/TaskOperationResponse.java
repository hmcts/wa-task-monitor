package uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

@EqualsAndHashCode
@ToString
public class TaskOperationResponse {

    private final Map<String, Object> responseMap;


    @JsonCreator
    public TaskOperationResponse(@JsonProperty("response_map") Map<String, Object> responseMap) {
        this.responseMap = responseMap;
    }

    public Map<String, Object> getResponseMap() {
        return responseMap;
    }

}
