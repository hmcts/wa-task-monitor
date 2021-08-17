package uk.gov.hmcts.reform.wataskmonitor.services;

import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;

public interface JobService {
    boolean canRun(JobName jobName);

    void run(String serviceToken, JobDetails jobDetails);
}
