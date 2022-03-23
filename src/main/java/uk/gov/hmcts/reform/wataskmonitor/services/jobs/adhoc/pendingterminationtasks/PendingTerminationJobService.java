package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.pendingterminationtasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.config.job.PendingTerminationJobConfig;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoryVariableInstance;
import uk.gov.hmcts.reform.wataskmonitor.utils.LoggingUtility;
import uk.gov.hmcts.reform.wataskmonitor.utils.ResourceUtility;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singleton;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.CFT_TASK_STATE;
import static uk.gov.hmcts.reform.wataskmonitor.services.ResourceEnum.CAMUNDA_HISTORIC_TASKS_PENDING_TERMINATION;

@Slf4j
@Component
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class PendingTerminationJobService {

    private final CamundaClient camundaClient;
    private final PendingTerminationJobConfig pendingTerminationJobConfig;

    public PendingTerminationJobService(CamundaClient camundaClient,
                                        PendingTerminationJobConfig pendingTerminationJobConfig) {
        this.camundaClient = camundaClient;
        this.pendingTerminationJobConfig = pendingTerminationJobConfig;
    }

    public void terminateTasks(String serviceAuthorizationToken) {
        List<HistoricCamundaTask> tasks = getTasksPendingTermination(serviceAuthorizationToken);
        deleteCftTaskState(serviceAuthorizationToken, tasks);
    }

    private List<HistoricCamundaTask> getTasksPendingTermination(String serviceToken) {
        log.info("Retrieving historic tasks pending termination from camunda.");
        log.info("pendingTerminationJobConfig: {}", pendingTerminationJobConfig.toString());

        String query = ResourceUtility.getResource(CAMUNDA_HISTORIC_TASKS_PENDING_TERMINATION);
        query = query.replace("\"finishedAfter\": \"*\",", "");
        log.info("Pending Termination Job build query : {}", LoggingUtility.logPrettyPrint(query));

        List<HistoricCamundaTask> camundaTasks = camundaClient.getTasksFromHistory(
            serviceToken,
            "0",
            pendingTerminationJobConfig.getCamundaMaxResults(),
            query
        );
        log.info("{} task(s) retrieved successfully.", camundaTasks.size());
        return camundaTasks;
    }

    private void deleteCftTaskState(String serviceAuthorizationToken,
                                    List<HistoricCamundaTask> tasks) {

        if (tasks.isEmpty()) {
            log.info("There were no task(s) to delete CFT Task state.");
        } else {
            log.info("Attempting to delete CFT Task state {} task(s)", tasks.size());
            tasks.forEach(task -> {
                try {
                    Map<String, Object> body = Map.of(
                        "variableName", CFT_TASK_STATE.value(),
                        "taskIdIn", singleton(task.getId())
                    );

                    List<HistoryVariableInstance> results = camundaClient.searchHistory(
                        serviceAuthorizationToken,
                        body
                    );

                    Optional<HistoryVariableInstance> maybeCftTaskState = results.stream()
                        .filter(r -> r.getName().equals(CFT_TASK_STATE.value()))
                        .findFirst();

                    maybeCftTaskState.ifPresent(
                        historyVariableInstance -> camundaClient.deleteVariableFromHistory(
                            serviceAuthorizationToken,
                            historyVariableInstance.getId()
                        )
                    );
                } catch (Exception e) {
                    log.error("Failed to delete pendingTermination CFT task state of task id {} with error: {}",
                              task.getId(),
                              e.getMessage());
                }
            });
        }
    }
}
