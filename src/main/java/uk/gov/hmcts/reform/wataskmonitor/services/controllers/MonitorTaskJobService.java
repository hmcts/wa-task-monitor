package uk.gov.hmcts.reform.wataskmonitor.services.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
    public CompletableFuture<String> execute(JobName jobName) throws ServerErrorException{
        String serviceToken = authTokenGenerator.generate();
        try {
            jobServices.forEach(job -> {
                if (job.canRun(jobName)) {
                    log.info("Running job '{}'", jobName.name());
                    job.run(serviceToken);
                }
            });
            return CompletableFuture.completedFuture(jobName.name() + " Job completed successfully");
        } catch (Exception e) {
            log.error("Error running job '{}'", jobName.name(), e);
            return CompletableFuture.failedFuture(e); // Return failed future with exception
        }
    }
}
