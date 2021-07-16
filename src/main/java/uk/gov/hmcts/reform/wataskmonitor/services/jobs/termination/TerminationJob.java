package uk.gov.hmcts.reform.wataskmonitor.services.jobs.termination;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.JobService;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.TERMINATION;

@Slf4j
@Component
public class TerminationJob implements JobService {
    private final TerminationJobService terminationJobService;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public TerminationJob(TerminationJobService terminationJobService,
                          AuthTokenGenerator authTokenGenerator) {
        this.terminationJobService = terminationJobService;
        this.authTokenGenerator = authTokenGenerator;
    }

    @Override
    public boolean canRun(JobName jobName) {
        return TERMINATION.equals(jobName);
    }

    @Override
    public void run() {
        log.info("Starting task termination job.");
        String serviceToken = authTokenGenerator.generate();
        List<HistoricCamundaTask> tasks = terminationJobService.getTasksPendingTermination(serviceToken);

        List<HistoricCamundaTask> completedTasks = tasks.stream()
            .filter(t -> t.getDeleteReason().equals("completed"))
            .collect(Collectors.toList());

        List<HistoricCamundaTask> cancelledTasks = tasks.stream()
            .filter(t -> t.getDeleteReason().equals("cancelled"))
            .collect(Collectors.toList());

        terminationJobService.terminateTasks(authTokenGenerator.generate(), completedTasks, cancelledTasks);
        log.info("Task termination job completed successfully.");
    }
}
