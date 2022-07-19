package uk.gov.hmcts.reform.wataskmonitor.services.jobs.cleanup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class CleanUpJobTest extends UnitBaseTest {

    public static final String CAMUNDA_DATE_REQUEST_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CAMUNDA_DATE_REQUEST_PATTERN);

    @Mock
    private CleanUpJobService cleanUpJobService;

    @InjectMocks
    private CleanUpJob cleanUpJob;

    @ParameterizedTest(name = "jobName: {0} expected: {1}")
    @CsvSource({
        "TERMINATION, false",
        "INITIATION, false",
        "CONFIGURATION, false",
        "AD_HOC_DELETE_PROCESS_INSTANCES, false",
        "TASK_CLEAN_UP, true",
    })
    void canRun(JobName jobName, boolean expectedResult) {

        assertThat(cleanUpJob.canRun(jobName)).isEqualTo(expectedResult);
    }

    @Test
    void should_run_when_environment_is_not_prod() {

        ZonedDateTime startTime = ZonedDateTime.now()
            .minusDays(7);

        ZonedDateTime endTime = ZonedDateTime.now()
            .minusDays(5);

        HistoricCamundaTask historicCamundaTask = new HistoricCamundaTask(
            "some taskId",
            "some delete reason",
            startTime.format(formatter),
            endTime.format(formatter)
        );
        List<HistoricCamundaTask> taskList = singletonList(historicCamundaTask);

        when(cleanUpJobService.isAllowedEnvironment())
            .thenReturn(true);

        when(cleanUpJobService.retrieveProcesses())
            .thenReturn(taskList);

        GenericJobReport jobReport = new GenericJobReport(
            1,
            singletonList(GenericJobOutcome.builder()
                .taskId(null)
                .processInstanceId("some processInstanceId")
                .successful(true)
                .jobType("Task Initiation")
                .build())
        );

        when(cleanUpJobService.deleteActiveProcesses(taskList, SOME_SERVICE_TOKEN))
            .thenReturn(jobReport);

        when(cleanUpJobService.deleteHistoricProcesses(taskList, SOME_SERVICE_TOKEN))
            .thenReturn(jobReport);

        cleanUpJob.run(SOME_SERVICE_TOKEN);

        verify(cleanUpJobService).retrieveProcesses();
        verify(cleanUpJobService).deleteActiveProcesses(taskList, SOME_SERVICE_TOKEN);
        verify(cleanUpJobService).deleteHistoricProcesses(taskList, SOME_SERVICE_TOKEN);
    }

    @Test
    void should_not_run_when_environment_is_prod() {

        when(cleanUpJobService.isAllowedEnvironment())
            .thenReturn(false);

        cleanUpJob.run(SOME_SERVICE_TOKEN);

        verify(cleanUpJobService, times(1)).isAllowedEnvironment();
        verify(cleanUpJobService, never()).retrieveProcesses();
        verify(cleanUpJobService, never()).deleteActiveProcesses(any(), any());
        verify(cleanUpJobService, never()).deleteHistoricProcesses(any(), any());
    }
}
