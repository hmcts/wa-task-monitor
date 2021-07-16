package uk.gov.hmcts.reform.wataskmonitor.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.JobService;

import java.util.List;

@Component
@Slf4j
public class MonitorTaskJobService {
    private final List<JobService> jobServices;

    @Autowired
    public MonitorTaskJobService(List<JobService> jobServices) {
        this.jobServices = jobServices;
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    @Async
    public void execute(JobName jobName) {
        jobServices.forEach(handler -> {
            if (handler.canHandle(jobName)) {
                log.info("Running job '{}", jobName.name());
                handler.run();
            }
        });
    }
}
