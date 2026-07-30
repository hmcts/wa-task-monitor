package uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.config.LaunchDarklyFeatureFlagProvider;
import uk.gov.hmcts.reform.wataskmonitor.config.features.FeatureFlag;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InitiationJobTest extends UnitBaseTest {

    @Mock
    private CamundaService camundaService;
    @Mock
    private InitiationService initiationService;
    @Mock
    private LaunchDarklyFeatureFlagProvider launchDarklyFeatureFlagProvider;
    @InjectMocks
    private InitiationJob initiationJob;

    @ParameterizedTest(name = "jobName: {0} expected: {1}")
    @CsvSource({
        "TERMINATION, false",
        "INITIATION, true",
        "AD_HOC_DELETE_PROCESS_INSTANCES, false"
    })
    void canRun(JobName jobName, boolean expectedResult) {
        assertThat(initiationJob.canRun(jobName)).isEqualTo(expectedResult);
    }

    @Test
    void run_when_launch_darkly_flag_is_disabled() {
        CamundaTask camundaTask = new CamundaTask(
            "some taskId",
            "some name",
            "someProcessInstanceId"
        );
        List<CamundaTask> taskList = singletonList(camundaTask);
        when(launchDarklyFeatureFlagProvider.getBooleanValue(FeatureFlag.WA_INITIATE_TASKS_ON_CREATE))
            .thenReturn(false);
        when(camundaService.getUnconfiguredTasks(SOME_SERVICE_TOKEN))
            .thenReturn(taskList);
        GenericJobReport jobReport = new GenericJobReport(
            1,
            singletonList(GenericJobOutcome.builder()
                              .taskId("some taskId")
                              .processInstanceId("some processInstanceId")
                              .successful(true)
                              .jobType("Task Initiation")
                              .build())
        );
        when(initiationService.initiateTasks(taskList, SOME_SERVICE_TOKEN, "Task Initiation"))
            .thenReturn(jobReport);

        initiationJob.run(SOME_SERVICE_TOKEN);

        verify(camundaService).getUnconfiguredTasks(SOME_SERVICE_TOKEN);
        verify(initiationService).initiateTasks(taskList, SOME_SERVICE_TOKEN, "Task Initiation");
    }

    @Test
    void should_skip_when_launch_darkly_flag_is_enabled() {
        when(launchDarklyFeatureFlagProvider.getBooleanValue(FeatureFlag.WA_INITIATE_TASKS_ON_CREATE))
            .thenReturn(true);

        initiationJob.run(SOME_SERVICE_TOKEN);

        verify(camundaService, never()).getUnconfiguredTasks(SOME_SERVICE_TOKEN);
        verify(initiationService, never()).initiateTasks(any(), any(), any());
    }
}
