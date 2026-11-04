package uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private static final String JOB_TYPE = "Task Initiation";

    private final CamundaService camundaService;
    private final InitiationService initiationService;
    private final boolean initiateTasksOnCreate;

    @Autowired
    public InitiationJob(CamundaService camundaService,
                         InitiationService initiationService,
                         @Value("${configuration.initiateTasksOnCreate:false}")
                         boolean initiateTasksOnCreate) {
        this.camundaService = camundaService;
        this.initiationService = initiationService;
        this.initiateTasksOnCreate = initiateTasksOnCreate;
    }

    @Override
    public boolean canRun(JobName jobName) {
        return INITIATION.equals(jobName);
    }

    @Override
    public void run(String serviceToken) {
        if (initiateTasksOnCreate) {
            log.info("{} job skipped because immediate task initiation is enabled.", INITIATION);
            return;
        }

        log.info("Starting task {} job.", INITIATION);
        List<CamundaTask> tasks = camundaService.getInitiationCandidates(serviceToken);
        GenericJobReport report = initiationService.initiateTasks(tasks, serviceToken, JOB_TYPE);
        log.info("{} job finished successfully: {}", INITIATION, logPrettyPrint(report));
    }
}
