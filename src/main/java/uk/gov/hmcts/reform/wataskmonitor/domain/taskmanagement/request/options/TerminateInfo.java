package uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TerminateReason;

@EqualsAndHashCode
@ToString
public class TerminateInfo {

    private final TerminateReason terminateReason;

    @JsonCreator
    public TerminateInfo(TerminateReason terminateReason) {
        this.terminateReason = terminateReason;
    }

    public TerminateReason getTerminateReason() {
        return terminateReason;
    }
}
