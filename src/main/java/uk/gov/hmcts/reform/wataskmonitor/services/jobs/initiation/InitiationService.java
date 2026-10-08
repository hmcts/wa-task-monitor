package uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskManagementClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaVariable;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.InitiateTaskRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.InitiateTaskOperation.INITIATION;

@Service
@Slf4j
public class InitiationService {

    private final CamundaService camundaService;
    private final TaskManagementClient taskManagementClient;
    private final InitiationTaskAttributesMapper initiationTaskAttributesMapper;

    public InitiationService(CamundaService camundaService,
                             TaskManagementClient taskManagementClient,
                             InitiationTaskAttributesMapper initiationTaskAttributesMapper) {
        this.camundaService = camundaService;
        this.taskManagementClient = taskManagementClient;
        this.initiationTaskAttributesMapper = initiationTaskAttributesMapper;
    }

    public GenericJobReport initiateTasks(List<CamundaTask> tasks,
                                          String serviceToken,
                                          String jobType) {
        if (tasks.isEmpty()) {
            log.info("There were no tasks to initiate.");
            return new GenericJobReport(0, emptyList());
        }

        List<GenericJobOutcome> outcomes = initiateTasksAndReturnOutcomes(
            tasks,
            serviceToken,
            jobType
        );
        return new GenericJobReport(tasks.size(), outcomes);
    }

    private List<GenericJobOutcome> initiateTasksAndReturnOutcomes(List<CamundaTask> tasks,
                                                                   String serviceToken,
                                                                   String jobType) {
        log.info("Attempting to initiate {} task(s).", tasks.size());
        List<GenericJobOutcome> outcomes = new ArrayList<>();
        tasks.forEach(task -> {
            try {
                log.info("Attempting to initiate task with id: '{}'.", task.getId());
                Map<String, CamundaVariable> variables = camundaService.getTaskVariables(
                    serviceToken,
                    task.getId()
                );
                Map<String, Object> taskAttributes = initiationTaskAttributesMapper.mapTaskAttributes(task, variables);
                taskManagementClient.initiateTask(
                    serviceToken,
                    task.getId(),
                    new InitiateTaskRequest(INITIATION, taskAttributes)
                );
                log.info("Task with id: '{}' initiated successfully.", task.getId());
                outcomes.add(buildJobOutcome(task, true, jobType));
            } catch (Exception exception) {
                log.error(
                    "Error while initiating taskId({}) and processId({})",
                    task.getId(),
                    task.getProcessInstanceId(),
                    exception
                );
                outcomes.add(buildJobOutcome(task, false, jobType));
            }
        });
        return outcomes;
    }

    private GenericJobOutcome buildJobOutcome(CamundaTask task,
                                               boolean successful,
                                               String jobType) {
        return GenericJobOutcome.builder()
            .taskId(task.getId())
            .processInstanceId(task.getProcessInstanceId())
            .successful(successful)
            .jobType(jobType)
            .build();
    }
}
