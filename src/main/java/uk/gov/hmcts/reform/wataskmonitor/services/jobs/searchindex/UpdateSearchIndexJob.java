package uk.gov.hmcts.reform.wataskmonitor.services.jobs.searchindex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.UPDATE_SEARCH_INDEX;

@Slf4j
@Component
public class UpdateSearchIndexJob implements JobService {
    private final UpdateSearchIndexJobService updateSearchIndexJobService;

    @Autowired
    public UpdateSearchIndexJob(UpdateSearchIndexJobService updateSearchIndexJobService) {
        this.updateSearchIndexJobService = updateSearchIndexJobService;
    }

    @Override
    public boolean canRun(JobName jobName) {
        return UPDATE_SEARCH_INDEX.equals(jobName);
    }

    @Override
    public void run(String serviceToken) {
        log.info("Starting {} job.", UPDATE_SEARCH_INDEX);
        String operationId = updateSearchIndexJobService.updateSearchIndex(serviceToken);
        log.info("{} job finished successfully: {}", UPDATE_SEARCH_INDEX, " for operationId:" + operationId);
    }
}
