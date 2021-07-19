package uk.gov.hmcts.reform.wataskmonitor.services.jobs;

import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.CaseIdList;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.RetrieveCaseIdListParam;

public interface RetrieveCaseIdListService<T extends RetrieveCaseIdListParam> {
    CaseIdList getCaseIdList(T param);
}
