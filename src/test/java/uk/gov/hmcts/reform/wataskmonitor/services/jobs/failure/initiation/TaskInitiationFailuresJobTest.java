package uk.gov.hmcts.reform.wataskmonitor.services.jobs.failure.initiation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation.CamundaService;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation.InitiationService;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.TASK_INITIATION_FAILURES;

@ExtendWith(MockitoExtension.class)
class TaskInitiationFailuresJobTest {

    private static final String SOME_SERVICE_TOKEN = "some service token";

    @Mock
    private TaskInitiationFailuresLogService taskInitiationFailuresLogService;
    @Mock
    private CamundaService camundaService;
    @Mock
    private InitiationService initiationService;

    private TaskInitiationFailuresJob taskInitiationFailuresJob;

    @BeforeEach
    void setUp() {
        taskInitiationFailuresJob = new TaskInitiationFailuresJob(
            taskInitiationFailuresLogService,
            camundaService,
            initiationService,
            false
        );
    }

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
        taskInitiationFailuresJob = new TaskInitiationFailuresJob(
            taskInitiationFailuresLogService,
            camundaService,
            initiationService,
            true
        );
        List<CamundaTask> tasks = List.of(new CamundaTask(
            "some taskId",
            "some name",
            "some processInstanceId"
        ));

        GenericJobReport jobReport = new GenericJobReport(
            1,
            singletonList(GenericJobOutcome.builder()
                .taskId("some taskId")
                .processInstanceId("some processInstanceId")
                .successful(true)
                .jobType(TASK_INITIATION_FAILURES.name())
                .build())
        );

        when(camundaService.getStaleUnconfiguredTasks(SOME_SERVICE_TOKEN))
            .thenReturn(tasks);
        when(initiationService.initiateTasks(
            tasks,
            SOME_SERVICE_TOKEN,
            TASK_INITIATION_FAILURES.name()
        ))
            .thenReturn(jobReport);
        taskInitiationFailuresJob.run(SOME_SERVICE_TOKEN);

        verify(camundaService).getStaleUnconfiguredTasks(SOME_SERVICE_TOKEN);
        verify(initiationService).initiateTasks(
            tasks,
            SOME_SERVICE_TOKEN,
            TASK_INITIATION_FAILURES.name()
        );
        verify(taskInitiationFailuresLogService, never()).reportInitiationFailures(tasks, SOME_SERVICE_TOKEN);
    }

    @Test
    void should_report_tasks_that_the_sweeper_could_not_initiate() {
        taskInitiationFailuresJob = new TaskInitiationFailuresJob(
            taskInitiationFailuresLogService,
            camundaService,
            initiationService,
            true
        );
        CamundaTask initiatedTask = new CamundaTask(
            "initiated taskId",
            "some name",
            "some processInstanceId"
        );
        CamundaTask failedTask = new CamundaTask(
            "failed taskId",
            "some name",
            "some processInstanceId"
        );
        List<CamundaTask> tasks = List.of(initiatedTask, failedTask);
        GenericJobReport jobReport = new GenericJobReport(
            2,
            List.of(
                GenericJobOutcome.builder()
                    .taskId(initiatedTask.getId())
                    .successful(true)
                    .build(),
                GenericJobOutcome.builder()
                    .taskId(failedTask.getId())
                    .successful(false)
                    .build()
            )
        );

        when(camundaService.getStaleUnconfiguredTasks(SOME_SERVICE_TOKEN)).thenReturn(tasks);
        when(initiationService.initiateTasks(
            tasks,
            SOME_SERVICE_TOKEN,
            TASK_INITIATION_FAILURES.name()
        )).thenReturn(jobReport);

        taskInitiationFailuresJob.run(SOME_SERVICE_TOKEN);

        verify(taskInitiationFailuresLogService).reportInitiationFailures(
            List.of(failedTask),
            SOME_SERVICE_TOKEN
        );
    }

    @Test
    void should_report_initiation_failures_when_feature_flag_is_disabled() {
        List<CamundaTask> tasks = List.of(new CamundaTask(
            "some taskId",
            "some name",
            "some processInstanceId"
        ));
        GenericJobReport jobReport = new GenericJobReport(0, List.of());

        when(camundaService.getStaleUnconfiguredTasks(SOME_SERVICE_TOKEN))
            .thenReturn(tasks);
        when(taskInitiationFailuresLogService.reportInitiationFailures(tasks, SOME_SERVICE_TOKEN))
            .thenReturn(jobReport);
        taskInitiationFailuresJob.run(SOME_SERVICE_TOKEN);

        verify(camundaService).getStaleUnconfiguredTasks(SOME_SERVICE_TOKEN);
        verify(taskInitiationFailuresLogService).reportInitiationFailures(tasks, SOME_SERVICE_TOKEN);
        verify(initiationService, never()).initiateTasks(
            tasks,
            SOME_SERVICE_TOKEN,
            TASK_INITIATION_FAILURES.name()
        );
    }
}
