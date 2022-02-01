package uk.gov.hmcts.reform.wataskmonitor.services.jobs.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskConfigurationClient;
import uk.gov.hmcts.reform.wataskmonitor.config.job.ConfigurationJobConfig;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
import uk.gov.hmcts.reform.wataskmonitor.utils.LoggingUtility;
import uk.gov.hmcts.reform.wataskmonitor.utils.ResourceUtility;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.wataskmonitor.services.ResourceEnum.CAMUNDA_TASKS_UNCONFIGURED;

@Component
@Slf4j
public class ConfigurationJobService {

    public static final String CAMUNDA_DATE_REQUEST_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private final boolean configurationTimeLimitFlag;
    private final long configurationTimeLimit;

    private final CamundaClient camundaClient;
    private final TaskConfigurationClient taskConfigurationClient;
    private final ConfigurationJobConfig configurationJobConfig;

    @Autowired
    public ConfigurationJobService(CamundaClient camundaClient,
                                   TaskConfigurationClient taskConfigurationClient,
                                   ConfigurationJobConfig configurationJobConfig,
                                   @Value("${job.configuration.camunda-time-limit-flag}")
                                       boolean configurationTimeLimitFlag,
                                   @Value("${job.configuration.camunda-time-limit}")
                                       long configurationTimeLimit) {
        this.camundaClient = camundaClient;
        this.taskConfigurationClient = taskConfigurationClient;
        this.configurationJobConfig = configurationJobConfig;
        this.configurationTimeLimitFlag = configurationTimeLimitFlag;
        this.configurationTimeLimit = configurationTimeLimit;
    }

    public boolean isConfigurationTimeLimitFlag() {
        return configurationTimeLimitFlag;
    }

    public long getConfigurationTimeLimit() {
        return configurationTimeLimit;
    }

    public List<CamundaTask> getUnConfiguredTasks(String serviceToken) {
        log.info("Retrieving tasks with '{}' = '{}' from camunda.", "taskState", "unconfigured");
        List<CamundaTask> camundaTasks = camundaClient.getTasks(
            serviceToken,
            "0",
            configurationJobConfig.getCamundaMaxResults(),
            buildSearchQuery()
        );
        log.info("{} task(s) retrieved successfully.", camundaTasks.size());
        return camundaTasks;
    }


    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public GenericJobReport configureTasks(List<CamundaTask> camundaTasks, String serviceToken) {
        if (camundaTasks.isEmpty()) {
            log.info("There was no task(s) to configure.");
            return new GenericJobReport(0, emptyList());
        } else {
            List<GenericJobOutcome> outcomesList = configureTasksAndReturnOutcome(camundaTasks, serviceToken);
            return new GenericJobReport(camundaTasks.size(), outcomesList);
        }
    }

    private List<GenericJobOutcome> configureTasksAndReturnOutcome(List<CamundaTask> camundaTasks,
                                                                   String serviceToken) {
        log.info("Attempting to configure {} task(s)", camundaTasks.size());
        List<GenericJobOutcome> outcomeList = new ArrayList<>();
        camundaTasks.forEach(task -> {
            try {
                log.info("Attempting to configure task with id: '{}'", task.getId());
                taskConfigurationClient.configureTask(serviceToken, task.getId());
                log.info("Task with id: '{}' configured successfully.", task.getId());
                outcomeList.add(buildJobOutcome(task, true));
            } catch (Exception e) {
                log.error("Error while configuring task with id: '{}'", task.getId());
                outcomeList.add(buildJobOutcome(task, false));
            }
        });
        return outcomeList;
    }

    private GenericJobOutcome buildJobOutcome(CamundaTask task, boolean isSuccessful) {
        return GenericJobOutcome.builder()
            .taskId(task.getId())
            .processInstanceId(task.getProcessInstanceId())
            .successful(isSuccessful)
            .jobType("Task Creation")
            .build();
    }

    private String buildSearchQuery() {
        String query = ResourceUtility.getResource(CAMUNDA_TASKS_UNCONFIGURED);

        query = query.replace("CREATED_BEFORE_PLACEHOLDER", getCreatedBeforeDate(5));

        if (isConfigurationTimeLimitFlag()) {
            String createdAfter = getCreatedBeforeDate(getConfigurationTimeLimit());
            query = query
                .replace("\"createdAfter\": \"*\",", "\"createdAfter\": \"" + createdAfter + "\",");
        } else {
            query = query
                .replace("\"createdAfter\": \"*\",", "");
        }

        log.info("Configuration Job build query : {}", LoggingUtility.logPrettyPrint(query));
        return query;
    }

    private static String getCreatedBeforeDate(long minutes) {
        return ZonedDateTime.now()
            .minusMinutes(minutes)
            .format(DateTimeFormatter.ofPattern(CAMUNDA_DATE_REQUEST_PATTERN));
    }

}
