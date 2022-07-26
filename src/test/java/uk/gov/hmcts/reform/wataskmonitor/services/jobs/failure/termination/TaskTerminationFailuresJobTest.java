package uk.gov.hmcts.reform.wataskmonitor.services.jobs.failure.termination;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class TaskTerminationFailuresJobTest extends UnitBaseTest {

    @Mock
    private TaskTerminationFailuresJobService taskTerminationFailuresJobService;
    @InjectMocks
    private TaskTerminationFailuresJob taskTerminationFailuresJob;

    @ParameterizedTest(name = "jobName: {0} expected: {1}")
    @CsvSource({
        "TERMINATION, false",
        "INITIATION, false",
        "CONFIGURATION, false",
        "AD_HOC_DELETE_PROCESS_INSTANCES, false",
        "TASK_INITIATION_FAILURES, false",
        "TASK_TERMINATION_FAILURES, true"
    })
    void can_run(JobName jobName, boolean expectedResult) {
        assertThat(taskTerminationFailuresJob.canRun(jobName)).isEqualTo(expectedResult);
    }

    @Test
    void run() {
        taskTerminationFailuresJob.run(SOME_SERVICE_TOKEN);
        verify(taskTerminationFailuresJobService).checkUnTerminatedTasks(SOME_SERVICE_TOKEN);
    }
}
