package uk.gov.hmcts.reform.wataskmonitor.services.jobs.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.models.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetailName;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.JobService;
import uk.gov.hmcts.reform.wataskmonitor.services.taskconfiguration.TaskConfigurationService;

import java.util.List;

@Slf4j
@Component
public class ConfigurationJob implements JobService {
    private final ConfigurationJobService configurationJobService;
    private final TaskConfigurationService taskConfigurationService;

    public ConfigurationJob(ConfigurationJobService configurationJobService,
                            TaskConfigurationService taskConfigurationService) {
        this.configurationJobService = configurationJobService;
        this.taskConfigurationService = taskConfigurationService;
    }

    @Override
    @SuppressWarnings("PMD.LawOfDemeter")
    public boolean canRun(JobDetailName jobDetailName) {
        return JobDetailName.CONFIGURATION.equals(jobDetailName);
    }

    @Override
    public void run(String serviceToken) {
        log.info("Starting " + JobDetailName.CONFIGURATION + ".");
        List<CamundaTask> tasks = configurationJobService.getUnConfiguredTasks(serviceToken);
        taskConfigurationService.configureTasks(tasks, serviceToken);
        log.info(JobDetailName.CONFIGURATION + " finished successfully.");
    }
}
