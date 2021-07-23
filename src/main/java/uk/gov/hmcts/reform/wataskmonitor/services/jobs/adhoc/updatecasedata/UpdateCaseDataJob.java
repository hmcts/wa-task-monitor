package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.updatecasedata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.JobService;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.AD_HOC_UPDATE_CASE_DATA;

@Slf4j
@Component
public class UpdateCaseDataJob implements JobService {

    @Override
    @SuppressWarnings("PMD.LawOfDemeter")
    public boolean canRun(JobName jobName) {
        return AD_HOC_UPDATE_CASE_DATA.equals(jobName);
    }

    @Override
    public void run(String serviceToken) {
        log.info("Starting '{}'", AD_HOC_UPDATE_CASE_DATA);
        log.info("{} finished successfully.", AD_HOC_UPDATE_CASE_DATA);
    }

}
