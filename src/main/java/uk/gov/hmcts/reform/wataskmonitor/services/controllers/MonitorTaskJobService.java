package uk.gov.hmcts.reform.wataskmonitor.services.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;

import java.util.List;

@Component
@Slf4j
@Import({AuthTokenGenerator.class})
public class MonitorTaskJobService {
    private final List<JobService> jobServices;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public MonitorTaskJobService(List<JobService> jobServices, AuthTokenGenerator authTokenGenerator) {
        this.jobServices = jobServices;
        this.authTokenGenerator = authTokenGenerator;
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    @Async
    public void execute(JobName jobName) {
        String serviceToken = authTokenGenerator.generate();
        jobServices.forEach(job -> {
            if (job.canRun(jobName)) {
                log.info("Running job '{}'", jobName.name());
                job.run(serviceToken);
            }
        });
    }
}
