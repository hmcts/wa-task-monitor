package uk.gov.hmcts.reform.wataskmonitor.services.jobs.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.models.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetailName;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.JobService;
import uk.gov.hmcts.reform.wataskmonitor.services.taskconfiguration.TaskConfigurationService;

import java.util.List;

@Slf4j
@Component
public class ConfigurationJob implements JobService {
    public static final String TASK_CONFIGURATION_JOB = "task configuration job";
    private final ConfigurationJobService configurationJobService;
    private final TaskConfigurationService taskConfigurationService;
    private final AuthTokenGenerator authTokenGenerator;

    public ConfigurationJob(ConfigurationJobService configurationJobService,
                            TaskConfigurationService taskConfigurationService,
                            AuthTokenGenerator authTokenGenerator) {
        this.configurationJobService = configurationJobService;
        this.taskConfigurationService = taskConfigurationService;
        this.authTokenGenerator = authTokenGenerator;
    }

    @Override
    @SuppressWarnings("PMD.LawOfDemeter")
    public boolean canRun(JobDetailName jobDetailName) {
        return JobDetailName.CONFIGURATION.equals(jobDetailName);
    }

    @Override
    public void run() {
        log.info("Starting " + TASK_CONFIGURATION_JOB + ".");
        String serviceToken = authTokenGenerator.generate();
        List<CamundaTask> tasks = configurationJobService.getUnConfiguredTasks(serviceToken);
        taskConfigurationService.configureTasks(tasks, serviceToken);
        log.info(TASK_CONFIGURATION_JOB + " finished successfully.");
    }
}
