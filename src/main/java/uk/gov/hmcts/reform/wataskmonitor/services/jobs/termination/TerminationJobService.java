package uk.gov.hmcts.reform.wataskmonitor.services.jobs.termination;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskManagementClient;
import uk.gov.hmcts.reform.wataskmonitor.config.job.TerminationJobConfig;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.TerminateTaskRequest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.options.TerminateInfo;
import uk.gov.hmcts.reform.wataskmonitor.utils.LoggingUtility;
import uk.gov.hmcts.reform.wataskmonitor.utils.ResourceUtility;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static uk.gov.hmcts.reform.wataskmonitor.services.ResourceEnum.CAMUNDA_HISTORIC_TASKS_PENDING_TERMINATION;

@Slf4j
@Component
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class TerminationJobService {
    public static final String CAMUNDA_DATE_REQUEST_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CAMUNDA_DATE_REQUEST_PATTERN);

    private final CamundaClient camundaClient;
    private final TaskManagementClient taskManagementClient;
    private final TerminationJobConfig terminationJobConfig;

    private final boolean terminationTimeLimitFlag;
    private final long terminationTimeLimit;

    public TerminationJobService(CamundaClient camundaClient,
                                 TaskManagementClient taskManagementClient,
                                 TerminationJobConfig terminationJobConfig,
                                 @Value("${job.termination.camunda-time-limit-flag}")
                                     boolean terminationTimeLimitFlag,
                                 @Value("${job.termination.camunda-time-limit}")
                                     long terminationTimeLimit) {
        this.camundaClient = camundaClient;
        this.taskManagementClient = taskManagementClient;
        this.terminationJobConfig = terminationJobConfig;
        this.terminationTimeLimitFlag = terminationTimeLimitFlag;
        this.terminationTimeLimit = terminationTimeLimit;
    }

    public void terminateTasks(String serviceAuthorizationToken) {
        List<HistoricCamundaTask> tasks = getTasksPendingTermination(serviceAuthorizationToken);
        terminateAllTasks(serviceAuthorizationToken, tasks);
    }

    private List<HistoricCamundaTask> getTasksPendingTermination(String serviceToken) {
        log.info("Retrieving historic tasks pending termination from camunda.");
        List<HistoricCamundaTask> camundaTasks = camundaClient.getTasksFromHistory(
            serviceToken,
            "0",
            terminationJobConfig.getCamundaMaxResults(),
            buildHistoricTasksPendingTerminationRequest()
        );
        log.info("{} task(s) retrieved successfully.", camundaTasks.size());
        return camundaTasks;
    }

    private void terminateAllTasks(String serviceAuthorizationToken,
                                   List<HistoricCamundaTask> tasks) {

        if (tasks.isEmpty()) {
            log.info("There were no task(s) to terminate.");
        } else {
            log.info("Attempting to terminate {} task(s)", tasks.size());
            tasks.forEach(task -> {
                TerminateTaskRequest request = new TerminateTaskRequest(new TerminateInfo(task.getDeleteReason()));
                try {
                    log.info(
                        "Attempting to terminate task with id: '{}' and reason '{}'",
                        task.getId(), task.getDeleteReason());
                    taskManagementClient.terminateTask(serviceAuthorizationToken, task.getId(), request);
                    log.info("Task with id: '{}' terminated successfully.", task.getId());
                } catch (Exception e) {
                    log.error(
                        "Error while terminating task with id: '{}' and reason '{}'",
                        task.getId(), task.getDeleteReason());
                }
            });
        }
    }

    private String buildHistoricTasksPendingTerminationRequest() {
        String query = ResourceUtility.getResource(CAMUNDA_HISTORIC_TASKS_PENDING_TERMINATION);

        if (isTerminationTimeLimitFlag()) {
            ZonedDateTime endTime = ZonedDateTime.now().minusMinutes(getTerminationTimeLimit());
            String finishedAfter = endTime.format(formatter);
            query = query
                .replace("\"finishedAfter\": \"*\",", "\"finishedAfter\": \""
                                                      + finishedAfter + "\",");
        } else {
            query = query
                .replace("\"finishedAfter\": \"*\",", "");
        }

        log.info("Termination Job build query : {}", LoggingUtility.logPrettyPrint(query));
        return query;
    }

    public boolean isTerminationTimeLimitFlag() {
        return terminationTimeLimitFlag;
    }

    public long getTerminationTimeLimit() {
        return terminationTimeLimit;
    }
}
