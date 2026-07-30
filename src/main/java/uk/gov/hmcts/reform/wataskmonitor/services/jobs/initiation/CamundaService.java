package uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.config.job.InitiationJobConfig;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaVariable;
import uk.gov.hmcts.reform.wataskmonitor.utils.LoggingUtility;
import uk.gov.hmcts.reform.wataskmonitor.utils.ResourceUtility;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.wataskmonitor.services.ResourceEnum.CAMUNDA_TASKS_CFT_TASK_STATE_UNCONFIGURED;

@Service
@Slf4j
public class CamundaService {

    public static final String CAMUNDA_DATE_REQUEST_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CAMUNDA_DATE_REQUEST_PATTERN);

    private final CamundaClient camundaClient;
    private final InitiationJobConfig initiationJobConfig;

    public CamundaService(CamundaClient camundaClient,
                          InitiationJobConfig initiationJobConfig) {
        this.camundaClient = camundaClient;
        this.initiationJobConfig = initiationJobConfig;
    }

    public List<CamundaTask> getUnconfiguredTasks(String serviceToken) {
        log.info("Retrieving tasks with '{}' = '{}' from Camunda.", "cftTaskState", "unconfigured");
        return getTasks(serviceToken, buildInitiationSearchQuery());
    }

    public List<CamundaTask> getStaleUnconfiguredTasks(String serviceToken) {
        if (!initiationJobConfig.isCamundaTimeLimitFlag()) {
            log.info("Camunda time limit flag is set to false.");
            return emptyList();
        }
        return getTasks(serviceToken, buildInitiationFailuresSearchQuery());
    }

    public Map<String, CamundaVariable> getTaskVariables(String serviceToken, String taskId) {
        return camundaClient.getVariables(serviceToken, taskId);
    }

    private List<CamundaTask> getTasks(String serviceToken, String searchQuery) {
        log.info("initiationJobConfig: {}", initiationJobConfig);
        List<CamundaTask> tasks = camundaClient.getTasks(
            serviceToken,
            "0",
            initiationJobConfig.getCamundaMaxResults(),
            searchQuery
        );
        log.info("{} task(s) retrieved successfully.", tasks.size());
        return tasks;
    }

    private String buildInitiationSearchQuery() {
        String query = ResourceUtility.getResource(CAMUNDA_TASKS_CFT_TASK_STATE_UNCONFIGURED)
            .replace("\"createdBefore\": \"*\",", "");

        if (initiationJobConfig.isCamundaTimeLimitFlag()) {
            ZonedDateTime createdTime = ZonedDateTime.now()
                .minusMinutes(initiationJobConfig.getCamundaTimeLimit());
            String createdAfter = createdTime.format(formatter);
            query = query.replace(
                "\"createdAfter\": \"*\",",
                "\"createdAfter\": \"" + createdAfter + "\","
            );
        } else {
            query = query.replace("\"createdAfter\": \"*\",", "");
        }

        log.info("Initiation build query: {}", LoggingUtility.logPrettyPrint(query));
        return query;
    }

    private String buildInitiationFailuresSearchQuery() {
        String query = ResourceUtility.getResource(CAMUNDA_TASKS_CFT_TASK_STATE_UNCONFIGURED)
            .replace("\"createdAfter\": \"*\",", "");

        ZonedDateTime createdTime = ZonedDateTime.now()
            .minusMinutes(initiationJobConfig.getCamundaTimeLimit());
        String createdBefore = createdTime.format(formatter);
        query = query.replace(
            "\"createdBefore\": \"*\",",
            "\"createdBefore\": \"" + createdBefore + "\","
        );

        log.info("Initiation failures build query: {}", LoggingUtility.logPrettyPrint(query));
        return query;
    }
}
