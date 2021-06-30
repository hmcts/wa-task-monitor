package uk.gov.hmcts.reform.wataskmonitor.schedulers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.models.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.services.camunda.CamundaService;
import uk.gov.hmcts.reform.wataskmonitor.services.taskconfiguration.TaskConfigurationService;

import java.util.List;

@Component
@Slf4j
@ConditionalOnProperty(
    value = "task.configurator.scheduling.enable", havingValue = "true"
)
public class TaskConfiguratorScheduler {

    private final CamundaService camundaService;
    private final TaskConfigurationService taskConfigurationService;
    private final AuthTokenGenerator authTokenGenerator;

    public TaskConfiguratorScheduler(CamundaService camundaService,
                                     TaskConfigurationService taskConfigurationService,
                                     AuthTokenGenerator authTokenGenerator) {
        this.camundaService = camundaService;
        this.taskConfigurationService = taskConfigurationService;
        this.authTokenGenerator = authTokenGenerator;
    }

    @Scheduled(fixedRateString = "${task.configurator.scheduling.fixedRate}")
    public void runTaskConfigurator() {
        log.info("Starting task configurator.");
        String serviceToken = authTokenGenerator.generate();
        List<CamundaTask> camundaTasks = camundaService.getUnConfiguredTasks(serviceToken);
        taskConfigurationService.configureTasks(camundaTasks, serviceToken);
        log.info("Task configurator finished successfully.");
    }
}
