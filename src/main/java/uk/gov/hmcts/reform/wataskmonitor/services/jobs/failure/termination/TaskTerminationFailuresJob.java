package uk.gov.hmcts.reform.wataskmonitor.services.jobs.failure.termination;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.TASK_TERMINATION_FAILURES;

@Slf4j
@Component
public class TaskTerminationFailuresJob implements JobService {
    private final TaskTerminationFailuresJobService taskTerminationFailuresJobService;

    @Autowired
    public TaskTerminationFailuresJob(TaskTerminationFailuresJobService taskTerminationFailuresJobService) {
        this.taskTerminationFailuresJobService = taskTerminationFailuresJobService;
    }

    @Override
    public boolean canRun(JobName jobName) {
        return TASK_TERMINATION_FAILURES.equals(jobName);
    }

    @Override
    public void run(String serviceToken) {
        log.info("Starting task {} job.", TASK_TERMINATION_FAILURES);
        taskTerminationFailuresJobService.checkUnTerminatedTasks(serviceToken);
        log.info("{} job completed successfully.", TASK_TERMINATION_FAILURES);

    }
}
