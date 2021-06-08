package uk.gov.hmcts.reform.wataskmonitor.schedulers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.models.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.services.CamundaService;
import uk.gov.hmcts.reform.wataskmonitor.services.TaskConfigurationService;

import java.util.List;

@Component
@Slf4j
@ConditionalOnProperty(
    value = "task.configurator.scheduling.enable", havingValue = "true"
)
public class TaskConfiguratorScheduler {

    private final CamundaService camundaService;
    private final TaskConfigurationService taskConfigurationService;

    public TaskConfiguratorScheduler(CamundaService camundaService,
                                     TaskConfigurationService taskConfigurationService) {
        this.camundaService = camundaService;
        this.taskConfigurationService = taskConfigurationService;
    }

    @Scheduled(fixedRateString = "${task.configurator.scheduling.fixedRate}")
    public void runTaskConfigurator() {
        log.info("Task configurator starts...");
        List<CamundaTask> camundaTasks = camundaService.getUnConfiguredTasks();
        taskConfigurationService.configureTasks(camundaTasks);
        log.info("Task configurator ends...");
    }
}
