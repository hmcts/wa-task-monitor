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
            log.warn("{} There are some unterminated tasks", TASK_TERMINATION_FAILURES.name());

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

    }

    private String buildHistoricTasksPendingTerminationRequest() {
        String query = ResourceUtility.getResource(CAMUNDA_TASKS_TERMINATION_FAILURES);

        if (terminationJobConfig.isCamundaTimeLimitFlag()) {
            ZonedDateTime endTime = ZonedDateTime.now()
                .minusMinutes(terminationJobConfig.getCamundaTimeLimit());
            String finishedBefore = endTime.format(formatter);
            query = query
                .replace("\"finishedBefore\": \"*\",", "\"finishedBefore\": \""
                                                       + finishedBefore + "\",");
        } else {
            query = query
                .replace("\"finishedBefore\": \"*\",", "");
        }

        log.info("{} build query : {}", TASK_TERMINATION_FAILURES.name(), LoggingUtility.logPrettyPrint(query));
        return query;
    }

}
