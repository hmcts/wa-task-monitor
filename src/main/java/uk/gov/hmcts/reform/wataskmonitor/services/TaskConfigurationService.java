package uk.gov.hmcts.reform.wataskmonitor.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskConfigurationClient;
import uk.gov.hmcts.reform.wataskmonitor.models.CamundaTask;

import java.util.List;

@Component
@Slf4j
public class TaskConfigurationService {

    private final TaskConfigurationClient taskConfigurationClient;
    private final AuthTokenGenerator authTokenGenerator;

    public TaskConfigurationService(TaskConfigurationClient taskConfigurationClient,
                                    AuthTokenGenerator authTokenGenerator) {
        this.taskConfigurationClient = taskConfigurationClient;
        this.authTokenGenerator = authTokenGenerator;
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public void configureTasks(List<CamundaTask> camundaTasks) {
        String serviceToken = authTokenGenerator.generate();
        camundaTasks.forEach(task -> {
            log.info("Configuring tasks...");
            try {
                taskConfigurationClient.configureTask(serviceToken, task.getId());
                log.info("Task(taskId={}) configured successfully.", task.getId());
            } catch (Exception e) {
                log.info("Error configuring task(taskId={}).", task.getId());
            }
        });
    }
}
