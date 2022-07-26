package uk.gov.hmcts.reform.wataskmonitor.domain.camunda;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class CamundaRemoveRequest {

    private String deleteReason;
    private List<String> processInstanceIds;

    private CamundaRemoveRequest() {
    }

    public CamundaRemoveRequest(String deleteReason, List<String> processInstanceIds) {
        this.deleteReason = deleteReason;
        this.processInstanceIds = processInstanceIds;
    }

    public String getDeleteReason() {
        return deleteReason;
    }

    public List<String> getProcessInstanceIds() {
        return processInstanceIds;
    }

}

