package uk.gov.hmcts.reform.wataskmonitor.services.jobs.reconfiguration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.RECONFIGURATION;

@Slf4j
@Component
public class ReconfigurationJob implements JobService {
    private final ReconfigurationJobService reconfigurationJobService;

    @Autowired
    public ReconfigurationJob(ReconfigurationJobService configurationJobService) {
        this.reconfigurationJobService = configurationJobService;
    }

    @Override
    public boolean canRun(JobName jobName) {
        return RECONFIGURATION.equals(jobName);
    }

    @Override
    public void run(String serviceToken) {
        log.info("Starting {} job.", RECONFIGURATION);
        String operationId = reconfigurationJobService.reconfigureTask(serviceToken);
        log.info("{} job finished successfully: {}", RECONFIGURATION, " for operationId:" + operationId);
    }
}
