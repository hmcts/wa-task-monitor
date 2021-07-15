package uk.gov.hmcts.reform.wataskmonitor.services.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.CamundaService;
import uk.gov.hmcts.reform.wataskmonitor.services.TaskConfigurationService;

import java.util.List;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.CONFIGURATION;

@Slf4j
@Component
public class ConfigurationJobHandler implements JobHandler {
    private final CamundaService camundaService;
    private final TaskConfigurationService taskConfigurationService;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public ConfigurationJobHandler(CamundaService camundaService,
                                   TaskConfigurationService taskConfigurationService,
                                   AuthTokenGenerator authTokenGenerator) {
        this.camundaService = camundaService;
        this.taskConfigurationService = taskConfigurationService;
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
        List<CamundaTask> tasks = camundaService.getUnConfiguredTasks(serviceToken);
        taskConfigurationService.configureTasks(tasks, serviceToken);
        log.info("Task configuration job finished successfully.");
    }
}
