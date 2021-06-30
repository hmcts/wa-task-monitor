package uk.gov.hmcts.reform.wataskmonitor.services.jobs;

import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetailName;

public class DeleteProcessInstancesJob implements JobService {
    @Override
    @SuppressWarnings("PMD.LawOfDemeter")
    public boolean canRun(JobDetailName jobDetailName) {
        return JobDetailName.AD_HOC.equals(jobDetailName);
    }

    @Override
    public void run() {

    }
}
