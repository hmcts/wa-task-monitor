package uk.gov.hmcts.reform.wataskmonitor.services.jobs;

import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.JobOutcome;

public interface JobOutcomeService {

    JobOutcome getJobOutcome(String serviceToken, String caseId);
}
