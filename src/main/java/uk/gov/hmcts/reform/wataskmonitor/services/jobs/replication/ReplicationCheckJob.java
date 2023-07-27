package uk.gov.hmcts.reform.wataskmonitor.services.jobs.replication;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.PERFORM_REPLICATION_CHECK;

@Slf4j
@Component
public class ReplicationCheckJob implements JobService {
    private final ReplicationCheckJobService replicationCheckJobService;

    @Autowired
    public ReplicationCheckJob(ReplicationCheckJobService replicationCheckJobService) {
        this.replicationCheckJobService = replicationCheckJobService;
    }

    @Override
    public boolean canRun(JobName jobName) {
        return PERFORM_REPLICATION_CHECK.equals(jobName);
    }

    @Override
    public void run(String serviceToken) {
        if (canRun(PERFORM_REPLICATION_CHECK)) {
            log.info("Starting {} job.", PERFORM_REPLICATION_CHECK);
            String operationId = replicationCheckJobService.replicationCheck(serviceToken);
            log.info("{} job finished successfully: {}", PERFORM_REPLICATION_CHECK, " for operationId:" + operationId);
        }
    }
}
