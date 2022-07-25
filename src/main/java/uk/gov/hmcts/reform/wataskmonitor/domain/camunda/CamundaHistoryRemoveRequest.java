package uk.gov.hmcts.reform.wataskmonitor.domain.camunda;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class CamundaHistoryRemoveRequest {

    private String deleteReason;
    private List<String> historicProcessInstanceIds;

    private CamundaHistoryRemoveRequest() {
    }

    public CamundaHistoryRemoveRequest(String deleteReason, List<String> historicProcessInstanceIds) {
        this.deleteReason = deleteReason;
        this.historicProcessInstanceIds = historicProcessInstanceIds;
    }

    public String getDeleteReason() {
        return deleteReason;
    }

    public List<String> getHistoricProcessInstanceIds() {
        return historicProcessInstanceIds;
    }

}

