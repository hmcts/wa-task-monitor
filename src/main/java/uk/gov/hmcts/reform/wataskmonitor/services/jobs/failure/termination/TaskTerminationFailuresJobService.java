package uk.gov.hmcts.reform.wataskmonitor.services.jobs.failure.termination;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.config.job.TerminationJobConfig;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.utils.LoggingUtility;
import uk.gov.hmcts.reform.wataskmonitor.utils.ResourceUtility;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.TASK_TERMINATION_FAILURES;
import static uk.gov.hmcts.reform.wataskmonitor.services.ResourceEnum.CAMUNDA_TASKS_TERMINATION_FAILURES;

@Slf4j
@Component
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class TaskTerminationFailuresJobService {
    public static final String CAMUNDA_DATE_REQUEST_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CAMUNDA_DATE_REQUEST_PATTERN);

    private final CamundaClient camundaClient;
    private final TerminationJobConfig terminationJobConfig;

    public TaskTerminationFailuresJobService(CamundaClient camundaClient,
                                             TerminationJobConfig terminationJobConfig) {
        this.camundaClient = camundaClient;
        this.terminationJobConfig = terminationJobConfig;
    }

    public void checkUnTerminatedTasks(String serviceToken) {
        log.info("{} terminationJobConfig: {}", TASK_TERMINATION_FAILURES.name(), terminationJobConfig.toString());

        if (terminationJobConfig.isCamundaTimeLimitFlag()) {
            List<HistoricCamundaTask> camundaTasks = camundaClient.getTasksFromHistory(
                serviceToken,
                "0",
                terminationJobConfig.getCamundaMaxResults(),
                buildHistoricTasksPendingTerminationRequest()
            );

            log.info("{} {} task(s) retrieved successfully.", TASK_TERMINATION_FAILURES.name(), camundaTasks.size());

            if (camundaTasks.isEmpty()) {
                log.info("{} There was no task", TASK_TERMINATION_FAILURES.name());
            } else {
                List<String> processIds = camundaTasks.stream().map(HistoricCamundaTask::getId)
                    .collect(Collectors.toList());
                log.warn("{} There are some unterminated tasks. Process Ids: {}",
                         TASK_TERMINATION_FAILURES.name(),
                         String.join(", ", processIds)
                );

                camundaTasks.forEach(task -> {
                    log.warn("{} -> taskId:{} deleteReason:{} startTime:{} endTime:{}",
                             TASK_TERMINATION_FAILURES.name(),
                             task.getId(),
                             task.getDeleteReason(),
                             task.getStartTime(),
                             task.getEndTime()
                    );
                });

            }
        } else {
            log.info("{} Time limit flag is set to false", TASK_TERMINATION_FAILURES.name());
        }

    }

    private String buildHistoricTasksPendingTerminationRequest() {
        String query = ResourceUtility.getResource(CAMUNDA_TASKS_TERMINATION_FAILURES);

        ZonedDateTime endTime = ZonedDateTime.now()
            .minusMinutes(terminationJobConfig.getCamundaTimeLimit());
        String finishedBefore = endTime.format(formatter);
        query = query
            .replace("\"finishedBefore\": \"*\",", "\"finishedBefore\": \""
                                                   + finishedBefore + "\",");

        log.info("{} build query : {}", TASK_TERMINATION_FAILURES.name(), LoggingUtility.logPrettyPrint(query));
        return query;
    }

}
