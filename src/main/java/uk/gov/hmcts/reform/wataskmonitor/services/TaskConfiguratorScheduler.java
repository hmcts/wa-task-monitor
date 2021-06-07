package uk.gov.hmcts.reform.wataskmonitor.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.models.Task;

import java.util.List;

@Component
@Slf4j
public class TaskConfiguratorScheduler {

    private final CamundaService camundaService;

    public TaskConfiguratorScheduler(CamundaService camundaService) {
        this.camundaService = camundaService;
    }

    //    @Scheduled(fixedRate = 10_000)
    public void runTaskConfigurator() {
        log.info("Task configurator starts...");

        List<Task> tasks = camundaService.getTasks();
        System.out.println(tasks);

        log.info("Task configurator ends...");
    }
}
