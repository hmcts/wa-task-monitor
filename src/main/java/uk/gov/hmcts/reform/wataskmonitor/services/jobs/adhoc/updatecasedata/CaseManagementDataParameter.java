package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.updatecasedata;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.updatecasedata.UpdateCaseDataParameter;

@ToString
@EqualsAndHashCode(callSuper = false)
@Getter
@Builder
public class CaseManagementDataParameter extends UpdateCaseDataParameter {
    private final String userAuthorization;
    private final String userId;
    private final String serviceAuthorization;
    private final String caseId;
}
