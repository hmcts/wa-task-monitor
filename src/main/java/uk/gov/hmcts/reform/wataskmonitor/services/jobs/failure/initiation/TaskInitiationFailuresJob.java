package uk.gov.hmcts.reform.wataskmonitor.services.jobs.failure.initiation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation.CamundaService;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation.InitiationService;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.TASK_INITIATION_FAILURES;
import static uk.gov.hmcts.reform.wataskmonitor.utils.LoggingUtility.logPrettyPrint;

@Slf4j
@Component
public class TaskInitiationFailuresJob implements JobService {
    private final TaskInitiationFailuresLogService taskInitiationFailuresLogService;
    private final CamundaService camundaService;
    private final InitiationService initiationService;
    private final boolean initiateTasksOnCreate;

    @Autowired
    public TaskInitiationFailuresJob(TaskInitiationFailuresLogService taskInitiationFailuresLogService,
                                     CamundaService camundaService,
                                     InitiationService initiationService,
                                     @Value("${configuration.initiateTasksOnCreate:false}")
                                     boolean initiateTasksOnCreate) {
        this.taskInitiationFailuresLogService = taskInitiationFailuresLogService;
        this.camundaService = camundaService;
        this.initiationService = initiationService;
        this.initiateTasksOnCreate = initiateTasksOnCreate;
    }

    @Override
    public boolean canRun(JobName jobName) {
        return TASK_INITIATION_FAILURES.equals(jobName);
    }

    @Override
    public void run(String serviceToken) {
        log.info("Starting task {} job.", TASK_INITIATION_FAILURES);
        List<CamundaTask> tasks = camundaService.getInitiationCandidates(serviceToken);
        GenericJobReport report;
        if (initiateTasksOnCreate) {
            report = initiationService.initiateTasks(
                tasks,
                serviceToken,
                TASK_INITIATION_FAILURES.name()
            );
            reportFailedInitiations(tasks, report, serviceToken);
        } else {
            report = taskInitiationFailuresLogService.reportInitiationFailures(tasks, serviceToken);
        }
        log.info("{} job completed successfully: {}", TASK_INITIATION_FAILURES, logPrettyPrint(report));
    }

    private void reportFailedInitiations(List<CamundaTask> tasks,
                                         GenericJobReport initiationReport,
                                         String serviceToken) {
        Set<String> failedTaskIds = initiationReport.getOutcomeList().stream()
            .filter(outcome -> !outcome.isSuccessful())
            .map(GenericJobOutcome::getTaskId)
            .collect(toSet());

        List<CamundaTask> failedTasks = tasks.stream()
            .filter(task -> failedTaskIds.contains(task.getId()))
            .toList();

        if (!failedTasks.isEmpty()) {
            taskInitiationFailuresLogService.reportInitiationFailures(failedTasks, serviceToken);
        }
    }
}
