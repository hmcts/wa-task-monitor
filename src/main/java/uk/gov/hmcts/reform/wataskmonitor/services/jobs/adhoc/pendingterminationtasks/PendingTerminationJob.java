package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.pendingterminationtasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.AD_HOC_PENDING_TERMINATION_TASKS;

@Slf4j
@Component
public class PendingTerminationJob implements JobService {
    private final PendingTerminationJobService pendingTerminationJobService;

    @Autowired
    public PendingTerminationJob(PendingTerminationJobService pendingTerminationJobService) {
        this.pendingTerminationJobService = pendingTerminationJobService;
    }

    @Override
    public boolean canRun(JobName jobName) {
        return AD_HOC_PENDING_TERMINATION_TASKS.equals(jobName);
    }

    @Override
    public void run(String serviceToken) {
        log.info("Starting task {} job.", AD_HOC_PENDING_TERMINATION_TASKS);
        pendingTerminationJobService.terminateTasks(serviceToken);
        log.info("Task pending termination job completed successfully.");
    }
}
