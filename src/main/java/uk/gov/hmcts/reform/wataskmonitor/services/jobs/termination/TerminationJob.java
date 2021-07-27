package uk.gov.hmcts.reform.wataskmonitor.services.jobs.termination;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.TERMINATION;

@Slf4j
@Component
public class TerminationJob implements JobService {
    private final TerminationJobService terminationJobService;

    @Autowired
    public TerminationJob(TerminationJobService terminationJobService) {
        this.terminationJobService = terminationJobService;
    }

    @Override
    public boolean canRun(JobName jobName) {
        return TERMINATION.equals(jobName);
    }

    @Override
    public void run(String serviceToken) {
        log.info("Starting task termination job.");
        terminationJobService.terminateTasks(serviceToken);
        log.info("Task termination job completed successfully.");

    }
}
