package uk.gov.hmcts.reform.wataskmonitor.services.jobs.maintenance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.DATABASE_MAINTENANCE;

@Slf4j
@Component
public class DatabaseMaintenanceJob implements JobService {

    private final DatabaseMaintenanceExecutorService databaseMaintenanceExecutorService;

    public DatabaseMaintenanceJob(DatabaseMaintenanceExecutorService databaseMaintenanceExecutorService) {
        this.databaseMaintenanceExecutorService = databaseMaintenanceExecutorService;
    }

    @Override
    public boolean canRun(JobName jobName) {
        return DATABASE_MAINTENANCE.equals(jobName);
    }

    @Override
    public void run(String serviceToken) {
        if (canRun(DATABASE_MAINTENANCE)) {
            log.info("Starting {} job.", DATABASE_MAINTENANCE);
            databaseMaintenanceExecutorService.executeConfiguredMaintenance();
            log.info("{} job finished successfully.", DATABASE_MAINTENANCE);
        }
    }
}
