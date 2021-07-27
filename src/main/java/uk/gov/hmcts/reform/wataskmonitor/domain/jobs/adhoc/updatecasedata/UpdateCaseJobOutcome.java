package uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.updatecasedata;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.JobOutcome;

@ToString
@EqualsAndHashCode(callSuper = false)
@Builder
@Getter
public class UpdateCaseJobOutcome extends JobOutcome {
    String caseId;
    boolean updated;
}
