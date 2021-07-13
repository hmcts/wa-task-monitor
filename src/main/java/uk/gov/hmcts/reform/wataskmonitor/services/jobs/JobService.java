package uk.gov.hmcts.reform.wataskmonitor.services.jobs;

import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetailName;

public interface JobService {
    boolean canRun(JobDetailName jobDetailName);

    void run(String serviceToken);
}
