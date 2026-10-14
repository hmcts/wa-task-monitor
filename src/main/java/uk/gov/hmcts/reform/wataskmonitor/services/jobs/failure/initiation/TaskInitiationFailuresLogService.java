package uk.gov.hmcts.reform.wataskmonitor.services.jobs.failure.initiation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaVariable;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation.CamundaService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.TASK_INITIATION_FAILURES;

@Service
@Slf4j
public class TaskInitiationFailuresLogService {

    private final CamundaService camundaService;

    public TaskInitiationFailuresLogService(CamundaService camundaService) {
        this.camundaService = camundaService;
    }

    public GenericJobReport reportInitiationFailures(List<CamundaTask> tasks,
                                                     String serviceToken) {
        if (tasks.isEmpty()) {
            log.info("{} There was no task", TASK_INITIATION_FAILURES.name());
            return new GenericJobReport(0, emptyList());
        }

        StringBuilder logMessage = new StringBuilder(TASK_INITIATION_FAILURES.name())
            .append(" There are some uninitiated tasks:\n");
        List<GenericJobOutcome> outcomes = new ArrayList<>();
        tasks.forEach(task -> {
            try {
                Map<String, CamundaVariable> variables = camundaService.getTaskVariables(serviceToken, task.getId());
                appendFailureDetails(logMessage, task, variables);

                log.warn("{} -> caseId:{} taskId:{} processInstanceId:{} taskState:{} cftTaskState:{} created:{}",
                         TASK_INITIATION_FAILURES.name(),
                         variables.get("caseId").getValue(),
                         task.getId(),
                         task.getProcessInstanceId(),
                         variables.get("taskState").getValue(),
                         variables.get("cftTaskState").getValue(),
                         task.getCreated());

                outcomes.add(buildJobOutcome(task, true));
            } catch (Exception exception) {
                log.error("{} Error while getting variable from Camunda taskId({}) and processId({})",
                          TASK_INITIATION_FAILURES.name(),
                          task.getId(),
                          task.getProcessInstanceId(),
                          exception);
                outcomes.add(buildJobOutcome(task, false));
            }
        });
        log.warn(logMessage.toString());
        return new GenericJobReport(tasks.size(), outcomes);
    }

    private void appendFailureDetails(StringBuilder logMessage,
                                      CamundaTask task,
                                      Map<String, CamundaVariable> variables) {
        logMessage.append(" -> caseId: ").append(variables.get("caseId").getValue())
            .append(", taskId: ").append(task.getId())
            .append(", jurisdiction: ").append(variables.get("jurisdiction").getValue())
            .append(", name: ").append(variables.get("name").getValue())
            .append(", caseType: ").append(variables.get("caseTypeId").getValue())
            .append(", created: ").append(task.getCreated())
            .append("\n");
    }

    private GenericJobOutcome buildJobOutcome(CamundaTask task, boolean isSuccessful) {
        return GenericJobOutcome.builder()
            .taskId(task.getId())
            .processInstanceId(task.getProcessInstanceId())
            .successful(isSuccessful)
            .jobType(TASK_INITIATION_FAILURES.name())
            .build();
    }
}
