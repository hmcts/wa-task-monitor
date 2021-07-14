package uk.gov.hmcts.reform.wataskmonitor.models.jobs.adhoc.createtasks;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobOutcome;

@ToString
@EqualsAndHashCode(callSuper = false)
@Builder
@Getter
public class CreateTaskJobOutcome extends JobOutcome {
    String caseId;
    String taskId;
    String processInstanceId;
    boolean created;
}
