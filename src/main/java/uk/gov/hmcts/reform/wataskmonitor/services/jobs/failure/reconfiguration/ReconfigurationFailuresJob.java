package uk.gov.hmcts.reform.wataskmonitor.services.jobs.failure.reconfiguration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.RECONFIGURATION_FAILURES;

@Slf4j
@Component
public class ReconfigurationFailuresJob implements JobService {
    private final ReconfigurationFailuresJobService reconfigurationFailureJobService;

    @Autowired
    public ReconfigurationFailuresJob(ReconfigurationFailuresJobService reconfigurationFailureJobService) {
        this.reconfigurationFailureJobService = reconfigurationFailureJobService;
    }

    @Override
    public boolean canRun(JobName jobName) {
        return RECONFIGURATION_FAILURES.equals(jobName);
    }

    @Override
    public void run(String serviceToken) {
        log.info("Starting {} job.", RECONFIGURATION_FAILURES);
        String operationId = reconfigurationFailureJobService.reconfigureFailuresTask(serviceToken);
        log.info("{} job finished successfully: {}", RECONFIGURATION_FAILURES, " for operationId:" + operationId);
    }
}
