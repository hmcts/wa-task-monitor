package uk.gov.hmcts.reform.wataskmonitor.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.handlers.JobHandler;

import java.util.List;

@Component
@Slf4j
public class MonitorTaskJobService {
    private final List<JobHandler> jobHandlers;

    @Autowired
    public MonitorTaskJobService(List<JobHandler> jobHandlers) {
        this.jobHandlers = jobHandlers;
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public void execute(JobName jobName) {
        jobHandlers.forEach(handler -> {
            if (handler.canHandle(jobName)) {
                log.info("Running job '{}", jobName.name());
                handler.run();
            }
        });
    }
}
