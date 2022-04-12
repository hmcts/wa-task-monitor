package uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskManagementClient;
import uk.gov.hmcts.reform.wataskmonitor.config.job.InitiationJobConfig;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaVariable;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.InitiateTaskRequest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.TaskAttribute;
import uk.gov.hmcts.reform.wataskmonitor.utils.LoggingUtility;
import uk.gov.hmcts.reform.wataskmonitor.utils.ResourceUtility;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.InitiateTaskOperation.INITIATION;
import static uk.gov.hmcts.reform.wataskmonitor.services.ResourceEnum.CAMUNDA_TASKS_CFT_TASK_STATE_UNCONFIGURED;

@Component
@Slf4j
public class InitiationJobService {

    public static final String CAMUNDA_DATE_REQUEST_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CAMUNDA_DATE_REQUEST_PATTERN);

    private final CamundaClient camundaClient;
    private final TaskManagementClient taskManagementClient;
    private final InitiationTaskAttributesMapper initiationTaskAttributesMapper;
    private final InitiationJobConfig initiationJobConfig;

    @Autowired
    public InitiationJobService(CamundaClient camundaClient,
                                TaskManagementClient taskManagementClient,
                                InitiationTaskAttributesMapper initiationTaskAttributesMapper,
                                InitiationJobConfig initiationJobConfig) {
        this.camundaClient = camundaClient;
        this.taskManagementClient = taskManagementClient;
        this.initiationTaskAttributesMapper = initiationTaskAttributesMapper;
        this.initiationJobConfig = initiationJobConfig;
    }

    public List<CamundaTask> getUnConfiguredTasks(String serviceToken) {
        log.info("Retrieving tasks with '{}' = '{}' from camunda.", "cftTaskState", "unconfigured");
        log.info("initiationJobConfig: {}", initiationJobConfig.toString());
        List<CamundaTask> camundaTasks = camundaClient.getTasks(
            serviceToken,
            "0",
            initiationJobConfig.getCamundaMaxResults(),
            buildSearchQuery()
        );
        log.info("{} task(s) retrieved successfully.", camundaTasks.size());
        return camundaTasks;
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public GenericJobReport initiateTasks(List<CamundaTask> camundaTasks, String serviceToken) {
        if (camundaTasks.isEmpty()) {
            log.info("There was no task(s) to initiate.");
            return new GenericJobReport(0, emptyList());
        } else {
            List<GenericJobOutcome> outcomesList = initiateTasksAndReturnOutcome(camundaTasks, serviceToken);
            return new GenericJobReport(camundaTasks.size(), outcomesList);
        }
    }

    private List<GenericJobOutcome> initiateTasksAndReturnOutcome(List<CamundaTask> camundaTasks,
                                                                  String serviceToken) {
        log.info("Attempting to initiate {} task(s)", camundaTasks.size());
        List<GenericJobOutcome> outcomeList = new ArrayList<>();
        camundaTasks.forEach(task -> {
            try {
                log.info("Attempting to initiate task with id: '{}'", task.getId());
                log.debug("-> Retrieving process variables for task with id: '{}'", task.getId());
                Map<String, CamundaVariable> variables = camundaClient.getVariables(
                    serviceToken,
                    task.getId()
                );
                List<TaskAttribute> taskAttributes = initiationTaskAttributesMapper.mapTaskAttributes(
                    task,
                    variables
                );
                log.debug("-> Initiating task with id: '{}'", task.getId());
                taskManagementClient.initiateTask(
                    serviceToken,
                    task.getId(),
                    new InitiateTaskRequest(INITIATION, taskAttributes)
                );
                log.info("Task with id: '{}' initiated successfully.", task.getId());
                outcomeList.add(buildJobOutcome(task, true));
            } catch (Exception e) {
                log.error("Error while initiating taskId({}) and processId({})",
                    task.getId(),
                    task.getProcessInstanceId(),
                    e);
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
            .jobType("Task Initiation")
            .build();
    }

    private String buildSearchQuery() {
        String query = ResourceUtility.getResource(CAMUNDA_TASKS_CFT_TASK_STATE_UNCONFIGURED);
        query = query
            .replace("\"createdBefore\": \"*\",", "");
        
        if (initiationJobConfig.isCamundaTimeLimitFlag()) {
            ZonedDateTime createdTime = ZonedDateTime.now()
                .minusMinutes(initiationJobConfig.getCamundaTimeLimit());
            String createdAfter = createdTime.format(formatter);
            query = query
                .replace("\"createdAfter\": \"*\",", "\"createdAfter\": \"" + createdAfter + "\",");
        } else {
            query = query
                .replace("\"createdAfter\": \"*\",", "");
        }

        log.info("Initiation Job build query : {}", LoggingUtility.logPrettyPrint(query));
        return query;
    }

}
