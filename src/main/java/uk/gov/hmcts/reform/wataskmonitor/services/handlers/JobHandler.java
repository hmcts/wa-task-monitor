package uk.gov.hmcts.reform.wataskmonitor.services.handlers;

import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

public interface JobHandler {
    boolean canHandle(JobName jobName);

    void run();
}
