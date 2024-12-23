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


class MaintenanceCamundaTaskCleanUpJobTest extends UnitBaseTest {

    public static final String CAMUNDA_DATE_REQUEST_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CAMUNDA_DATE_REQUEST_PATTERN);

    @Mock
    private MaintenanceCamundaTaskCleanUpJobService maintenanceCamundaTaskCleanUpJobService;

    @InjectMocks
    private MaintenanceCamundaTaskCleanUpJob maintenanceCamundaTaskCleanUpJob;

    @ParameterizedTest(name = "jobName: {0} expected: {1}")
    @CsvSource({
        "TERMINATION, false",
        "INITIATION, false",
        "AD_HOC_DELETE_PROCESS_INSTANCES, false",
        "MAINTENANCE_CAMUNDA_TASK_CLEAN_UP, true",
    })
    void canRun(JobName jobName, boolean expectedResult) {

        assertThat(maintenanceCamundaTaskCleanUpJob.canRun(jobName)).isEqualTo(expectedResult);
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

        when(maintenanceCamundaTaskCleanUpJobService.isAllowedEnvironment())
            .thenReturn(true);

        when(maintenanceCamundaTaskCleanUpJobService.retrieveHistoricProcesses())
            .thenReturn(taskList);

        when(maintenanceCamundaTaskCleanUpJobService.retrieveActiveProcesses())
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

        when(maintenanceCamundaTaskCleanUpJobService.deleteActiveProcesses(taskList, SOME_SERVICE_TOKEN))
            .thenReturn(jobReport);

        when(maintenanceCamundaTaskCleanUpJobService.deleteHistoricProcesses(taskList, SOME_SERVICE_TOKEN))
            .thenReturn(jobReport);

        maintenanceCamundaTaskCleanUpJob.run(SOME_SERVICE_TOKEN);

        verify(maintenanceCamundaTaskCleanUpJobService).retrieveHistoricProcesses();
        verify(maintenanceCamundaTaskCleanUpJobService).retrieveActiveProcesses();
        verify(maintenanceCamundaTaskCleanUpJobService).deleteActiveProcesses(taskList, SOME_SERVICE_TOKEN);
        verify(maintenanceCamundaTaskCleanUpJobService).deleteHistoricProcesses(taskList, SOME_SERVICE_TOKEN);
    }

    @Test
    void should_not_run_when_environment_is_prod() {

        when(maintenanceCamundaTaskCleanUpJobService.isAllowedEnvironment())
            .thenReturn(false);

        maintenanceCamundaTaskCleanUpJob.run(SOME_SERVICE_TOKEN);

        verify(maintenanceCamundaTaskCleanUpJobService, times(1)).isAllowedEnvironment();
        verify(maintenanceCamundaTaskCleanUpJobService, never()).retrieveHistoricProcesses();
        verify(maintenanceCamundaTaskCleanUpJobService, never()).retrieveActiveProcesses();
        verify(maintenanceCamundaTaskCleanUpJobService, never()).deleteActiveProcesses(any(), any());
        verify(maintenanceCamundaTaskCleanUpJobService, never()).deleteHistoricProcesses(any(), any());
    }
}
