package uk.gov.hmcts.reform.wataskmonitor.services.jobs;

import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

public interface JobService {
    boolean canRun(JobName jobName);

    void run(String serviceToken);
}
