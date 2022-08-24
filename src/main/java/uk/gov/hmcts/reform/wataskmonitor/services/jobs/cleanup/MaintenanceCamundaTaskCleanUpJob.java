package uk.gov.hmcts.reform.wataskmonitor.services.jobs.cleanup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;

import java.util.List;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.MAINTENANCE_CAMUNDA_TASK_CLEAN_UP;

@Slf4j
@Component
public class MaintenanceCamundaTaskCleanUpJob implements JobService {

    private final MaintenanceCamundaTaskCleanUpJobService maintenanceCamundaTaskCleanUpJobService;

    @Autowired
    public MaintenanceCamundaTaskCleanUpJob(
        MaintenanceCamundaTaskCleanUpJobService maintenanceCamundaTaskCleanUpJobService) {
        this.maintenanceCamundaTaskCleanUpJobService = maintenanceCamundaTaskCleanUpJobService;
    }

    @Override
    public boolean canRun(JobName jobName) {
        return MAINTENANCE_CAMUNDA_TASK_CLEAN_UP.equals(jobName);
    }

    @Override
    public void run(String serviceToken) {

        if (!maintenanceCamundaTaskCleanUpJobService.isAllowedEnvironment()) {
            return;
        }

        log.info("Starting task {} job.", MAINTENANCE_CAMUNDA_TASK_CLEAN_UP);
        List<HistoricCamundaTask> tasks = maintenanceCamundaTaskCleanUpJobService.retrieveProcesses();

        maintenanceCamundaTaskCleanUpJobService.deleteActiveProcesses(tasks, serviceToken);
        log.info("{} active process deletion finished successfully", MAINTENANCE_CAMUNDA_TASK_CLEAN_UP);

        maintenanceCamundaTaskCleanUpJobService.deleteHistoricProcesses(tasks, serviceToken);
        log.info("{} history process deletion finished successfully", MAINTENANCE_CAMUNDA_TASK_CLEAN_UP);
        log.info("{} job finished successfully", MAINTENANCE_CAMUNDA_TASK_CLEAN_UP);

    }
}
