package uk.gov.hmcts.reform.wataskmonitor.services.jobs.termination;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class TerminationJobTest extends UnitBaseTest {

    @Mock
    private TerminationJobService terminationJobService;
    @InjectMocks
    private TerminationJob terminationJob;

    @ParameterizedTest(name = "jobName: {0} expected: {1}")
    @CsvSource({
        "TERMINATION, true",
        "INITIATION, false",
        "CONFIGURATION, false",
        "AD_HOC_DELETE_PROCESS_INSTANCES, false"
    })
    void can_run(JobName jobName, boolean expectedResult) {
        assertThat(terminationJob.canRun(jobName)).isEqualTo(expectedResult);
    }

    @Test
    void run() {
        terminationJob.run(SOME_SERVICE_TOKEN);
        verify(terminationJobService).terminateTasks(SOME_SERVICE_TOKEN);
    }
}
