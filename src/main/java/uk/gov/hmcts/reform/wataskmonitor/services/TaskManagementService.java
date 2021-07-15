package uk.gov.hmcts.reform.wataskmonitor.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskManagementClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.TerminateTaskRequest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TerminateReason;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.options.TerminateInfo;

import java.util.List;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TerminateReason.CANCELLED;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TerminateReason.COMPLETED;

@Component
@Slf4j
public class TaskManagementService {

    private final TaskManagementClient taskManagementClient;

    @Autowired
    public TaskManagementService(TaskManagementClient taskManagementClient) {
        this.taskManagementClient = taskManagementClient;
    }

    public void terminateTasks(String serviceAuthorizationToken,
                               List<HistoricCamundaTask> completedTasks,
                               List<HistoricCamundaTask> cancelledTasks) {
        terminateAllTasksWithReason(serviceAuthorizationToken, completedTasks, COMPLETED);
        terminateAllTasksWithReason(serviceAuthorizationToken, cancelledTasks, CANCELLED);
    }

    private void terminateAllTasksWithReason(String serviceAuthorizationToken,
                                             List<HistoricCamundaTask> tasks,
                                             TerminateReason reason) {

        if (tasks.isEmpty()) {
            log.info("There were no '{}' task(s) to terminate.", reason);
        } else {
            log.info("Attempting to terminate {} task(s) with reason '{}'", tasks.size(), reason);
            TerminateTaskRequest request = new TerminateTaskRequest(new TerminateInfo(reason));
            tasks.forEach(task -> {
                try {
                    log.info("Attempting to terminate task with id: '{}' and reason '{}'", task.getId(), reason);
                    taskManagementClient.terminateTask(serviceAuthorizationToken, task.getId(), request);
                    log.info("Task with id: '{}' terminated successfully.", task.getId());
                } catch (Exception e) {
                    log.info("Error while terminating task with id: '{}' and reason '{}'", task.getId(), reason);
                }
            });
        }
    }
}
