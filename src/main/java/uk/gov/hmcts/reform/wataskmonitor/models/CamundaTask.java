package uk.gov.hmcts.reform.wataskmonitor.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public final class CamundaTask {

    private final String id;

    public CamundaTask(@JsonProperty("id") String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
