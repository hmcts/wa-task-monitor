package uk.gov.hmcts.reform.wataskmonitor.services.jobs.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.JobService;

import java.util.List;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.CONFIGURATION;

@Slf4j
@Component
public class ConfigurationJob implements JobService {
    private final ConfigurationJobService configurationJobService;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public ConfigurationJob(ConfigurationJobService configurationJobService,
                            AuthTokenGenerator authTokenGenerator) {
        this.configurationJobService = configurationJobService;
        this.authTokenGenerator = authTokenGenerator;
    }

    @Override
    public boolean canHandle(JobName jobName) {
        return CONFIGURATION.equals(jobName);
    }

    @Override
    public void run() {
        log.info("Starting task configuration job.");
        String serviceToken = authTokenGenerator.generate();
        List<CamundaTask> tasks = configurationJobService.getUnConfiguredTasks(serviceToken);
        configurationJobService.configureTasks(tasks, serviceToken);
        log.info("Task configuration job finished successfully.");
    }
}
