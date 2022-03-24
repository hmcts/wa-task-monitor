package uk.gov.hmcts.reform.wataskmonitor.services.jobs.failure.initiation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.TASK_INITIATION_FAILURES;
import static uk.gov.hmcts.reform.wataskmonitor.utils.LoggingUtility.logPrettyPrint;

@Slf4j
@Component
public class TaskInitiationFailuresJob implements JobService {
    private final TaskInitiationFailuresJobService taskInitiationFailuresJobService;

    @Autowired
    public TaskInitiationFailuresJob(TaskInitiationFailuresJobService taskInitiationFailuresJobService) {
        this.taskInitiationFailuresJobService = taskInitiationFailuresJobService;
    }

    @Override
    public boolean canRun(JobName jobName) {
        return TASK_INITIATION_FAILURES.equals(jobName);
    }

    @Override
    public void run(String serviceToken) {
        log.info("Starting task {} job.", TASK_INITIATION_FAILURES);
        GenericJobReport report = taskInitiationFailuresJobService.getUnInitiatedTasks(serviceToken);
        log.info("{} job completed successfully: {}", TASK_INITIATION_FAILURES, logPrettyPrint(report));
    }
}
