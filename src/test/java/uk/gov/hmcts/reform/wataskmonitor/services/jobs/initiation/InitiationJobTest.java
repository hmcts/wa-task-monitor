package uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation;

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

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitiationJobTest {

    private static final String SOME_SERVICE_TOKEN = "some service token";

    @Mock
    private CamundaService camundaService;
    @Mock
    private InitiationService initiationService;
    private InitiationJob initiationJob;

    @BeforeEach
    void setUp() {
        initiationJob = new InitiationJob(camundaService, initiationService, false);
    }

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
    void should_run_when_immediate_task_initiation_is_disabled() {
        CamundaTask camundaTask = new CamundaTask(
            "some taskId",
            "some name",
            "someProcessInstanceId"
        );
        List<CamundaTask> taskList = singletonList(camundaTask);
        when(camundaService.getInitiationCandidates(SOME_SERVICE_TOKEN))
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

        verify(camundaService).getInitiationCandidates(SOME_SERVICE_TOKEN);
        verify(initiationService).initiateTasks(taskList, SOME_SERVICE_TOKEN, "Task Initiation");
    }

    @Test
    void should_skip_when_immediate_task_initiation_is_enabled() {
        initiationJob = new InitiationJob(camundaService, initiationService, true);

        initiationJob.run(SOME_SERVICE_TOKEN);

        verify(camundaService, never()).getInitiationCandidates(SOME_SERVICE_TOKEN);
        verify(initiationService, never()).initiateTasks(any(), any(), any());
    }
}
