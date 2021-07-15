package uk.gov.hmcts.reform.wataskmonitor.services.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.TerminateReason;
import uk.gov.hmcts.reform.wataskmonitor.services.CamundaService;
import uk.gov.hmcts.reform.wataskmonitor.services.TaskConfigurationService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.TERMINATION;

@Slf4j
@Component
public class TerminationJobHandler implements JobHandler {
    private final CamundaService camundaService;
    private final TaskConfigurationService taskConfigurationService;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public TerminationJobHandler(CamundaService camundaService,
                                 TaskConfigurationService taskConfigurationService,
                                 AuthTokenGenerator authTokenGenerator) {
        this.camundaService = camundaService;
        this.taskConfigurationService = taskConfigurationService;
        this.authTokenGenerator = authTokenGenerator;
    }

    @Override
    public boolean canHandle(JobName jobName) {
        return TERMINATION.equals(jobName);
    }

    @Override
    public void run() {
        log.info("Starting task termination job.");
        //Fetch tasks with state cftTaskstate = pendingTermination
        //For Each call history to find deleteReason
        // If completed:
        // Call task management /terminate with termination Reason Completed
        //If cancelled:
        // Call task management /terminate with termination Reason Cancelled
        String serviceToken = authTokenGenerator.generate();
        List<CamundaTask> tasks = camundaService.getTasksPendingTermination(serviceToken);
        Map<TerminateReason, List<CamundaTask>> tasksByTerminateReason = new ConcurrentHashMap<>();

        tasks.forEach( task -> {
            task.
        });
        taskConfigurationService.configureTasks(tasks, serviceToken);
        log.info("Task termination job completed successfully.");
    }
}
