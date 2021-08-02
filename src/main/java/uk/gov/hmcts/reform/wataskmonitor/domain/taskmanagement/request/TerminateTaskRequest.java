package uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.options.TerminateInfo;

@EqualsAndHashCode
@ToString
public class TerminateTaskRequest {

    private final TerminateInfo terminateInfo;

    @JsonCreator
    public TerminateTaskRequest(TerminateInfo terminateInfo) {
        this.terminateInfo = terminateInfo;
    }

    public TerminateInfo getTerminateInfo() {
        return terminateInfo;
    }
}
