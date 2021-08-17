package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.updatecasedata;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.AD_HOC_UPDATE_CASE_DATA;

class UpdateCaseDataJobTest extends UnitBaseTest {

    @Mock
    private UpdateCaseDataJobService updateCaseDataJobService;

    @InjectMocks
    private UpdateCaseDataJob updateCaseDataJob;

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void canRun() {
        Arrays.stream(JobName.values())
            .forEach(name ->
                         assertThat(updateCaseDataJob.canRun(name)).isEqualTo(AD_HOC_UPDATE_CASE_DATA.equals(name)));
    }

    @Test
    void run() {
        updateCaseDataJob.run(
            SOME_SERVICE_TOKEN,
            new JobDetails(AD_HOC_UPDATE_CASE_DATA, "1000")
        );

        Mockito.verify(updateCaseDataJobService).updateCcdCases(SOME_SERVICE_TOKEN);
    }
}
