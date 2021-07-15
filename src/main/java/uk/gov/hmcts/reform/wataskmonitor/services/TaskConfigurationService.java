package uk.gov.hmcts.reform.wataskmonitor.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskConfigurationClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;

import java.util.List;

@Component
@Slf4j
public class TaskConfigurationService {

    private final TaskConfigurationClient taskConfigurationClient;

    @Autowired
    public TaskConfigurationService(TaskConfigurationClient taskConfigurationClient) {
        this.taskConfigurationClient = taskConfigurationClient;
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public void configureTasks(List<CamundaTask> camundaTasks, String serviceToken) {
        if (camundaTasks.isEmpty()) {
            log.info("There was no task(s) to configure.");
        } else {
            log.info("Attempting to configure {} task(s)", camundaTasks.size());
            camundaTasks.forEach(task -> {
                try {
                    log.info("Attempting to configure task with id: '{}'", task.getId());
                    taskConfigurationClient.configureTask(serviceToken, task.getId());
                    log.info("Task with id: '{}' configured successfully.", task.getId());
                } catch (Exception e) {
                    log.info("Error while configuring task with id: '{}'", task.getId());
                }
            });
        }
    }
}
