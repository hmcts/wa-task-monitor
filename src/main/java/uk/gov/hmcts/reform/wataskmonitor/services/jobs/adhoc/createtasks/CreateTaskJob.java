package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetailName;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.JobService;

import static uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetailName.AD_HOC_CREATE_TASKS;

@Slf4j
@Component
public class CreateTaskJob implements JobService {

    @Override
    @SuppressWarnings("PMD.LawOfDemeter")
    public boolean canRun(JobDetailName jobDetailName) {
        return AD_HOC_CREATE_TASKS.equals(jobDetailName);
    }

    @Override
    public void run(String serviceToken) {
        log.info("Starting '{}'", AD_HOC_CREATE_TASKS);
        log.info("{} finished successfully.", AD_HOC_CREATE_TASKS);
    }

}
