package uk.gov.hmcts.reform.wataskmonitor.domain.camunda;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoricCamundaTask {

    private final String id;
    private final String deleteReason;
    private final String startTime;
    private final String endTime;

    public HistoricCamundaTask(@JsonProperty("id") String id,
                               @JsonProperty("deleteReason") String deleteReason,
                               @JsonProperty("startTime") String startTime,
                               @JsonProperty("endTime") String endTime) {
        this.id = id;
        this.deleteReason = deleteReason;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getId() {
        return id;
    }

    public String getDeleteReason() {
        return deleteReason;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}
