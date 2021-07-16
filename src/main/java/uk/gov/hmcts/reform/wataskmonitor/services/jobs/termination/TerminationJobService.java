package uk.gov.hmcts.reform.wataskmonitor.services.jobs.termination;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskManagementClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.TerminateTaskRequest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TerminateReason;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.options.TerminateInfo;
import uk.gov.hmcts.reform.wataskmonitor.utils.ResourceUtility;

import java.util.List;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TerminateReason.CANCELLED;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TerminateReason.COMPLETED;
import static uk.gov.hmcts.reform.wataskmonitor.services.jobs.RequestsEnum.CAMUNDA_HISTORIC_TASKS_PENDING_TERMINATION;

@Slf4j
@Component
public class TerminationJobService {

    private final CamundaClient camundaClient;
    private final TaskManagementClient taskManagementClient;


    @Autowired
    public TerminationJobService(CamundaClient camundaClient, TaskManagementClient taskManagementClient) {
        this.camundaClient = camundaClient;
        this.taskManagementClient = taskManagementClient;
    }

    public List<HistoricCamundaTask> getTasksPendingTermination(String serviceToken) {
        log.info("Retrieving historic tasks pending termination from camunda.");
        List<HistoricCamundaTask> camundaTasks = camundaClient.getTasksFromHistory(
            serviceToken,
            "0",
            "1000",
            buildHistoricTasksPendingTerminationRequest()
        );
        log.info("{} task(s) retrieved successfully.", camundaTasks.size());
        return camundaTasks;
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

    private String buildHistoricTasksPendingTerminationRequest() {
        return ResourceUtility.getResource(CAMUNDA_HISTORIC_TASKS_PENDING_TERMINATION.getRequestBodyLocation());
    }

}
