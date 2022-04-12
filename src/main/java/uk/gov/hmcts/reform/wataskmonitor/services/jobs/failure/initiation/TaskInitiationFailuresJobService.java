package uk.gov.hmcts.reform.wataskmonitor.services.jobs.failure.initiation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.config.job.InitiationJobConfig;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaVariable;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
import uk.gov.hmcts.reform.wataskmonitor.utils.LoggingUtility;
import uk.gov.hmcts.reform.wataskmonitor.utils.ResourceUtility;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.TASK_INITIATION_FAILURES;
import static uk.gov.hmcts.reform.wataskmonitor.services.ResourceEnum.CAMUNDA_TASKS_CFT_TASK_STATE_UNCONFIGURED;

@Component
@Slf4j
public class TaskInitiationFailuresJobService {

    public static final String CAMUNDA_DATE_REQUEST_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CAMUNDA_DATE_REQUEST_PATTERN);

    private final CamundaClient camundaClient;
    private final InitiationJobConfig initiationJobConfig;

    @Autowired
    public TaskInitiationFailuresJobService(CamundaClient camundaClient,
                                            InitiationJobConfig initiationJobConfig) {
        this.camundaClient = camundaClient;
        this.initiationJobConfig = initiationJobConfig;
    }

    public GenericJobReport getInitiationFailures(String serviceToken) {
        log.info("{} initiationJobConfig: {}", TASK_INITIATION_FAILURES.name(), initiationJobConfig.toString());
        List<CamundaTask> camundaTasks = camundaClient.getTasks(
            serviceToken,
            "0",
            initiationJobConfig.getCamundaMaxResults(),
            buildSearchQuery()
        );

        log.info("{} {} task(s) retrieved successfully.",
            TASK_INITIATION_FAILURES.name(), camundaTasks.size());

        if (camundaTasks.isEmpty()) {
            log.info("{} There was no task", TASK_INITIATION_FAILURES.name());
            return new GenericJobReport(0, emptyList());
        } else {
            log.warn("{} There are some uninitiated tasks", TASK_INITIATION_FAILURES.name());
            List<GenericJobOutcome> outcomesList = prepareInitiationFailureReport(camundaTasks, serviceToken);
            return new GenericJobReport(camundaTasks.size(), outcomesList);
        }

    }

    private List<GenericJobOutcome> prepareInitiationFailureReport(List<CamundaTask> camundaTasks,
                                                                   String serviceToken) {
        List<GenericJobOutcome> outcomeList = new ArrayList<>();
        camundaTasks.forEach(task -> {
            try {
                Map<String, CamundaVariable> variables = camundaClient.getVariables(
                    serviceToken,
                    task.getId()
                );

                log.warn("{} -> caseId:{} taskId:{} processInstanceId:{} taskState:{} cftTaskState:{} created:{}",
                    TASK_INITIATION_FAILURES.name(),
                    variables.get("caseId").getValue(),
                    task.getId(),
                    task.getProcessInstanceId(),
                    variables.get("taskState").getValue(),
                    variables.get("cftTaskState").getValue(),
                    task.getCreated()
                );

                outcomeList.add(buildJobOutcome(task, true));
            } catch (Exception e) {
                log.error("{} Error while getting variable from Camunda taskId({}) and processId({})",
                    TASK_INITIATION_FAILURES.name(),
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
            .jobType(TASK_INITIATION_FAILURES.name())
            .build();
    }

    private String buildSearchQuery() {
        String query = ResourceUtility.getResource(CAMUNDA_TASKS_CFT_TASK_STATE_UNCONFIGURED);
        query = query
            .replace("\"createdAfter\": \"*\",", "");

        if (initiationJobConfig.isCamundaTimeLimitFlag()) {
            ZonedDateTime createdTime = ZonedDateTime.now()
                .minusMinutes(initiationJobConfig.getCamundaTimeLimit());

            String createdBefore = createdTime.format(formatter);

            query = query
                .replace(
                    "\"createdBefore\": \"*\",", "\"createdBefore\": \"" + createdBefore + "\","
                );
        } else {
            query = query
                .replace("\"createdBefore\": \"*\",", "");
        }

        log.info("{} build query : {}", TASK_INITIATION_FAILURES.name(), LoggingUtility.logPrettyPrint(query));
        return query;
    }

}
