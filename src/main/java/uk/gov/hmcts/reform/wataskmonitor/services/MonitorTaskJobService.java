package uk.gov.hmcts.reform.wataskmonitor.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetailName;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.JobService;

import java.util.List;

@Component
@Slf4j
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
    public void execute(JobDetailName jobDetailName) {
        String serviceToken = authTokenGenerator.generate();
        jobServices.forEach(job -> {
            if (job.canRun(jobDetailName)) {
                log.info("Running job '{}'", jobDetailName.name());
                job.run(serviceToken);
            }
        });
    }
}
