package uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class TerminateInfo {

    private final String terminateReason;

    @JsonCreator
    public TerminateInfo(String terminateReason) {
        this.terminateReason = terminateReason;
    }

    public String getTerminateReason() {
        return terminateReason;
    }
}
