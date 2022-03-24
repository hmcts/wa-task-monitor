package uk.gov.hmcts.reform.wataskmonitor.services.jobs.failure.initiation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.TASK_INITIATION_FAILURES;

class TaskInitiationFailuresJobTest extends UnitBaseTest {

    @Mock
    private TaskInitiationFailuresJobService taskInitiationFailuresJobService;

    @InjectMocks
    private TaskInitiationFailuresJob taskInitiationFailuresJob;

    @ParameterizedTest(name = "jobName: {0} expected: {1}")
    @CsvSource({
        "TERMINATION, false",
        "INITIATION, false",
        "CONFIGURATION, false",
        "AD_HOC_DELETE_PROCESS_INSTANCES, false",
        "TASK_INITIATION_FAILURES, true",
        "TASK_TERMINATION_FAILURES, false"
    })
    void canRun(JobName jobName, boolean expectedResult) {
        assertThat(taskInitiationFailuresJob.canRun(jobName)).isEqualTo(expectedResult);
    }

    @Test
    void run() {

        GenericJobReport jobReport = new GenericJobReport(
            1,
            singletonList(GenericJobOutcome.builder()
                .taskId("some taskId")
                .processInstanceId("some processInstanceId")
                .successful(true)
                .jobType(TASK_INITIATION_FAILURES.name())
                .build())
        );

        when(taskInitiationFailuresJobService.getUnInitiatedTasks(SOME_SERVICE_TOKEN))
            .thenReturn(jobReport);

        taskInitiationFailuresJob.run(SOME_SERVICE_TOKEN);

        verify(taskInitiationFailuresJobService).getUnInitiatedTasks(SOME_SERVICE_TOKEN);
    }
}
