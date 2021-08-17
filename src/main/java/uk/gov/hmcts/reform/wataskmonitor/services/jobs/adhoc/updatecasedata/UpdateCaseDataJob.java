package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.updatecasedata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.JobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.AD_HOC_UPDATE_CASE_DATA;
import static uk.gov.hmcts.reform.wataskmonitor.utils.LoggingUtility.logPrettyPrint;

@Slf4j
@Component
public class UpdateCaseDataJob implements JobService {

    private final UpdateCaseDataJobService updateCaseDataJobService;

    public UpdateCaseDataJob(UpdateCaseDataJobService updateCaseDataJobService) {
        this.updateCaseDataJobService = updateCaseDataJobService;
    }

    @Override
    @SuppressWarnings("PMD.LawOfDemeter")
    public boolean canRun(JobName jobName) {
        return AD_HOC_UPDATE_CASE_DATA.equals(jobName);
    }

    @Override
    public void run(String serviceToken, JobDetails jobDetails) {
        log.info("Starting '{}'", AD_HOC_UPDATE_CASE_DATA);
        JobReport report = updateCaseDataJobService.updateCcdCases(serviceToken);
        log.info("{} finished successfully: {}", AD_HOC_UPDATE_CASE_DATA, logPrettyPrint(report));
    }

}
