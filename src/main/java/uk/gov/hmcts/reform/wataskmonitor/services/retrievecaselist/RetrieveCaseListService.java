package uk.gov.hmcts.reform.wataskmonitor.services.retrievecaselist;

import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.CaseIdList;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.RetrieveCaseListParam;

public interface RetrieveCaseListService<T extends RetrieveCaseListParam> {
    CaseIdList retrieveCaseList(T param);
}
