package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CreateTaskJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.JobService;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.AD_HOC_CREATE_TASKS;
import static uk.gov.hmcts.reform.wataskmonitor.utils.LoggingUtility.logPrettyPrint;

@Slf4j
@Component
public class CreateTaskJob implements JobService {

    private final CreateTaskJobService createTaskJobService;

    public CreateTaskJob(CreateTaskJobService createTaskJobService) {
        this.createTaskJobService = createTaskJobService;
    }

    @Override
    @SuppressWarnings("PMD.LawOfDemeter")
    public boolean canRun(JobName jobName) {
        return AD_HOC_CREATE_TASKS.equals(jobName);
    }

    @Override
    public void run(String serviceToken) {
        log.info("Starting '{}'", AD_HOC_CREATE_TASKS);
        CreateTaskJobReport tasks = createTaskJobService.createTasks(serviceToken);
        log.info("{} finished successfully: {}", AD_HOC_CREATE_TASKS, logPrettyPrint(tasks));
    }

}
