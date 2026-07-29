package uk.gov.hmcts.reform.wataskmonitor.services.jobs.failure.initiation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.config.LaunchDarklyFeatureFlagProvider;
import uk.gov.hmcts.reform.wataskmonitor.config.features.FeatureFlag;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.TASK_INITIATION_FAILURES;

class TaskInitiationFailuresJobTest extends UnitBaseTest {

    @Mock
    private TaskInitiationFailuresJobService taskInitiationFailuresJobService;
    @Mock
    private LaunchDarklyFeatureFlagProvider launchDarklyFeatureFlagProvider;

    @InjectMocks
    private TaskInitiationFailuresJob taskInitiationFailuresJob;

    @ParameterizedTest(name = "jobName: {0} expected: {1}")
    @CsvSource({
        "TERMINATION, false",
        "INITIATION, false",
        "AD_HOC_DELETE_PROCESS_INSTANCES, false",
        "TASK_INITIATION_FAILURES, true",
        "TASK_TERMINATION_FAILURES, false"
    })
    void canRun(JobName jobName, boolean expectedResult) {
        assertThat(taskInitiationFailuresJob.canRun(jobName)).isEqualTo(expectedResult);
    }

    @Test
    void should_initiate_failed_tasks_when_feature_flag_is_enabled() {

        GenericJobReport jobReport = new GenericJobReport(
            1,
            singletonList(GenericJobOutcome.builder()
                .taskId("some taskId")
                .processInstanceId("some processInstanceId")
                .successful(true)
                .jobType(TASK_INITIATION_FAILURES.name())
                .build())
        );

        when(taskInitiationFailuresJobService.initiateFailedTasks(SOME_SERVICE_TOKEN))
            .thenReturn(jobReport);
        when(launchDarklyFeatureFlagProvider.getBooleanValue(FeatureFlag.WA_INITIATE_TASKS_ON_CREATE))
            .thenReturn(true);

        taskInitiationFailuresJob.run(SOME_SERVICE_TOKEN);

        verify(taskInitiationFailuresJobService).initiateFailedTasks(SOME_SERVICE_TOKEN);
        verify(taskInitiationFailuresJobService, never()).getInitiationFailures(SOME_SERVICE_TOKEN);
    }

    @Test
    void should_report_initiation_failures_when_feature_flag_is_disabled() {
        GenericJobReport jobReport = new GenericJobReport(0, List.of());

        when(taskInitiationFailuresJobService.getInitiationFailures(SOME_SERVICE_TOKEN))
            .thenReturn(jobReport);
        when(launchDarklyFeatureFlagProvider.getBooleanValue(FeatureFlag.WA_INITIATE_TASKS_ON_CREATE))
            .thenReturn(false);

        taskInitiationFailuresJob.run(SOME_SERVICE_TOKEN);

        verify(taskInitiationFailuresJobService).getInitiationFailures(SOME_SERVICE_TOKEN);
        verify(taskInitiationFailuresJobService, never()).initiateFailedTasks(SOME_SERVICE_TOKEN);
    }
}
