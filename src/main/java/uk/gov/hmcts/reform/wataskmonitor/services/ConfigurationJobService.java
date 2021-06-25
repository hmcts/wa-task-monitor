package uk.gov.hmcts.reform.wataskmonitor.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.models.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.models.JobDetailName;

import java.util.List;

@Slf4j
@Component
public class ConfigurationJobService implements JobService {
    private final CamundaService camundaService;
    private final TaskConfigurationService taskConfigurationService;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public ConfigurationJobService(CamundaService camundaService,
                                   TaskConfigurationService taskConfigurationService,
                                   AuthTokenGenerator authTokenGenerator) {
        this.camundaService = camundaService;
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
        log.info("Starting task configuration job.");
        String serviceToken = authTokenGenerator.generate();
        List<CamundaTask> tasks = camundaService.getUnConfiguredTasks(serviceToken);
        taskConfigurationService.configureTasks(tasks, serviceToken);
        log.info("Task configuration job finished successfully.");
    }
}
