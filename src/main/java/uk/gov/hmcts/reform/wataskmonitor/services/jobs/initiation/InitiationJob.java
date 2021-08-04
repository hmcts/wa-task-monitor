package uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;

import java.util.List;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.INITIATION;
import static uk.gov.hmcts.reform.wataskmonitor.utils.LoggingUtility.logPrettyPrint;

@Slf4j
@Component
public class InitiationJob implements JobService {
    private final InitiationJobService initiationJobService;

    @Autowired
    public InitiationJob(InitiationJobService initiationJobService) {
        this.initiationJobService = initiationJobService;
    }

    @Override
    public boolean canRun(JobName jobName) {
        return INITIATION.equals(jobName);
    }

    @Override
    public void run(String serviceToken) {
        log.info("Starting task {} job.", INITIATION);
        List<CamundaTask> tasks = initiationJobService.getUnConfiguredTasks(serviceToken);
        GenericJobReport report = initiationJobService.initiateTasks(tasks, serviceToken);
        log.info("{} job finished successfully: {}", INITIATION, logPrettyPrint(report));
    }
}
