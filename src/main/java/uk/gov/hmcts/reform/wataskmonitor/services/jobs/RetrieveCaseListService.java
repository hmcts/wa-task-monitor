package uk.gov.hmcts.reform.wataskmonitor.services.jobs;

import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.CaseIdList;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.RetrieveCaseListParam;

public interface RetrieveCaseListService<T extends RetrieveCaseListParam> {
    CaseIdList getCaseList(T param);
}
