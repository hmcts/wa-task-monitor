package uk.gov.hmcts.reform.wataskmonitor.services.jobs.cleanupsensitivelogs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.CLEANUP_SENSITIVE_LOG_ENTRIES;

@Slf4j
@Component
public class CleanupSensitiveLogsJob implements JobService {
    private final CleanupSensitiveLogsJobService cleanupSensitiveLogsJobService;

    @Autowired
    public CleanupSensitiveLogsJob(CleanupSensitiveLogsJobService cleanupSensitiveLogsJobService) {
        this.cleanupSensitiveLogsJobService = cleanupSensitiveLogsJobService;
    }

    @Override
    public boolean canRun(JobName jobName) {
        return CLEANUP_SENSITIVE_LOG_ENTRIES.equals(jobName);
    }

    @Override
    public void run(String serviceToken) {
        log.info("Starting {} job.", CLEANUP_SENSITIVE_LOG_ENTRIES);
        String operationId = cleanupSensitiveLogsJobService.cleanSensitiveLogs(serviceToken);
        log.info("{} job finished successfully: {}", CLEANUP_SENSITIVE_LOG_ENTRIES, " for operationId:" + operationId);
    }
}
