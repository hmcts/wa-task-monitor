package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.pendingterminationtasks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class PendingTerminationJobTest extends UnitBaseTest {

    @Mock
    private PendingTerminationJobService pendingTerminationJobService;
    @InjectMocks
    private PendingTerminationJob pendingTerminationJob;

    @ParameterizedTest(name = "jobName: {0} expected: {1}")
    @CsvSource({
        "TERMINATION, false",
        "INITIATION, false",
        "CONFIGURATION, false",
        "AD_HOC_DELETE_PROCESS_INSTANCES, false",
        "AD_HOC_PENDING_TERMINATION_TASKS, true"
    })
    void canRun(JobName jobName, boolean expectedResult) {
        assertThat(pendingTerminationJob.canRun(jobName)).isEqualTo(expectedResult);
    }

    @Test
    void run() {
        pendingTerminationJob.run(SOME_SERVICE_TOKEN);

        verify(pendingTerminationJobService).terminateTasks(SOME_SERVICE_TOKEN);
    }
}
