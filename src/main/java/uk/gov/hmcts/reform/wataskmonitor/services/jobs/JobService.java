package uk.gov.hmcts.reform.wataskmonitor.services.jobs;

import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

public interface JobService {
    boolean canHandle(JobName jobName);

    void run();
}
