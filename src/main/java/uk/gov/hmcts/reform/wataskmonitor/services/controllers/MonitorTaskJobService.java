package uk.gov.hmcts.reform.wataskmonitor.services.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;

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
    public void execute(JobDetails jobDetails) {
        String serviceToken = authTokenGenerator.generate();
        jobServices.forEach(job -> {
            if (job.canRun(jobDetails.getName())) {
                log.info("Running job '{}'", jobDetails.getName());
                job.run(serviceToken, jobDetails);
            }
        });
    }
}
