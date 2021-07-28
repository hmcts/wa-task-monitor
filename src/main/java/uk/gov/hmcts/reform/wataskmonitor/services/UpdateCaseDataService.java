package uk.gov.hmcts.reform.wataskmonitor.services;

import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.updatecasedata.UpdateCaseDataParameter;

public interface UpdateCaseDataService<T extends UpdateCaseDataParameter> {

    boolean updateCaseInCcd(T parameter);
}
