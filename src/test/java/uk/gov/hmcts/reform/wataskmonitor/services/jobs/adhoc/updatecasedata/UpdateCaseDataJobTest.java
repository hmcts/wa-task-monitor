package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.updatecasedata;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.AD_HOC_UPDATE_CASE_DATA;

class UpdateCaseDataJobTest {

    private final UpdateCaseDataJob updateCaseDataJob = new UpdateCaseDataJob();

    @Test
    void canRun() {
        Arrays.stream(JobName.values())
            .forEach(name ->
                         assertThat(updateCaseDataJob.canRun(name)).isEqualTo(AD_HOC_UPDATE_CASE_DATA.equals(name)));
    }
}
