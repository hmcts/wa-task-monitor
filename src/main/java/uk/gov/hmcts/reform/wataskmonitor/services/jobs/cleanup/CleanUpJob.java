package uk.gov.hmcts.reform.wataskmonitor.services.jobs.cleanup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;

import java.util.List;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.TASK_CLEAN_UP;

@Slf4j
@Component
public class CleanUpJob implements JobService {

    private final CleanUpJobService cleanUpJobService;

    @Autowired
    public CleanUpJob(CleanUpJobService cleanUpJobService) {
        this.cleanUpJobService = cleanUpJobService;
    }

    @Override
    public boolean canRun(JobName jobName) {
        return TASK_CLEAN_UP.equals(jobName);
    }

    @Override
    public void run(String serviceToken) {

        if (!cleanUpJobService.isAllowedEnvironment()) {
            return;
        }

        log.info("Starting task {} job.", TASK_CLEAN_UP);
        List<HistoricCamundaTask> tasks = cleanUpJobService.retrieveProcesses();

        cleanUpJobService.deleteActiveProcesses(tasks, serviceToken);
        log.info("{} active process deletion finished successfully", TASK_CLEAN_UP);

        cleanUpJobService.deleteHistoricProcesses(tasks, serviceToken);
        log.info("{} history process deletion finished successfully", TASK_CLEAN_UP);
        log.info("{} job finished successfully", TASK_CLEAN_UP);

    }
}
