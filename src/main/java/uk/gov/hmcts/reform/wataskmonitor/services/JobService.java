package uk.gov.hmcts.reform.wataskmonitor.services;

import uk.gov.hmcts.reform.wataskmonitor.models.JobDetailName;

public interface JobService {
    boolean canRun(JobDetailName jobDetailName);

    void run();
}
