package uk.gov.hmcts.reform.wataskmonitor.domain.jobs;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
@Builder
@Getter
public class GenericJobOutcome implements JobOutcome {
    String taskId;
    String processInstanceId;
    boolean successful;
    String jobType;
}
