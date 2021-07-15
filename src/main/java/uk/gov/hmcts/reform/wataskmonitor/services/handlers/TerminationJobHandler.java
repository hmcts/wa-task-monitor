package uk.gov.hmcts.reform.wataskmonitor.services.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.CamundaService;
import uk.gov.hmcts.reform.wataskmonitor.services.TaskManagementService;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.TERMINATION;

@Slf4j
@Component
public class TerminationJobHandler implements JobHandler {
    private final CamundaService camundaService;
    private final TaskManagementService taskManagementService;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public TerminationJobHandler(CamundaService camundaService,
                                 TaskManagementService taskManagementService,
                                 AuthTokenGenerator authTokenGenerator) {
        this.camundaService = camundaService;
        this.taskManagementService = taskManagementService;
        this.authTokenGenerator = authTokenGenerator;
    }

    @Override
    public boolean canHandle(JobName jobName) {
        return TERMINATION.equals(jobName);
    }

    @Override
    public void run() {
        log.info("Starting task termination job.");
        String serviceToken = authTokenGenerator.generate();
        List<HistoricCamundaTask> tasks = camundaService.getTasksPendingTermination(serviceToken);

        List<HistoricCamundaTask> completedTasks = tasks.stream()
            .filter(t -> t.getDeleteReason().equals("completed"))
            .collect(Collectors.toList());

        List<HistoricCamundaTask> cancelledTasks = tasks.stream()
            .filter(t -> t.getDeleteReason().equals("cancelled"))
            .collect(Collectors.toList());

        taskManagementService.terminateTasks(authTokenGenerator.generate(), completedTasks, cancelledTasks);
        log.info("Task termination job completed successfully.");
    }
}
