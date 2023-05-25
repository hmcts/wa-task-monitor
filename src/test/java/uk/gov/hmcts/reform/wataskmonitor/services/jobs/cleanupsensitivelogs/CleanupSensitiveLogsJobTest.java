package uk.gov.hmcts.reform.wataskmonitor.services.jobs.cleanupsensitivelogs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.CLEANUP_SENSITIVE_LOG_ENTRIES;

@ExtendWith(OutputCaptureExtension.class)
class CleanupSensitiveLogsJobTest extends UnitBaseTest {

    @Mock
    private CleanupSensitiveLogsJobService cleanupSensitiveLogsJobService;

    @InjectMocks
    private CleanupSensitiveLogsJob cleanupSensitiveLogsJob;

    @ParameterizedTest(name = "jobName: {0} expected: {1}")
    @CsvSource({
        "TERMINATION, false",
        "INITIATION, false",
        "AD_HOC_DELETE_PROCESS_INSTANCES, false",
        "MAINTENANCE_CAMUNDA_TASK_CLEAN_UP, false",
        "CLEANUP_SENSITIVE_LOG_ENTRIES, true",
    })
    void canRun(JobName jobName, boolean expectedResult) {

        assertThat(cleanupSensitiveLogsJob.canRun(jobName)).isEqualTo(expectedResult);
    }

    @Test
    void should_run_and_write_some_logs(CapturedOutput output) {

        String operationId = UUID.randomUUID().toString();

        when(cleanupSensitiveLogsJobService.cleanSensitiveLogs(SOME_SERVICE_TOKEN))
            .thenReturn(operationId);

        cleanupSensitiveLogsJob.run(SOME_SERVICE_TOKEN);

        verify(cleanupSensitiveLogsJobService).cleanSensitiveLogs(SOME_SERVICE_TOKEN);

        String startingLog = String.format("Starting %s job.", CLEANUP_SENSITIVE_LOG_ENTRIES);
        String finishingLog = String.format("%s job finished successfully: %s",
            CLEANUP_SENSITIVE_LOG_ENTRIES, " for operationId:" + operationId);

        assertThat(output.getOut().contains(startingLog));
        assertThat(output.getOut().contains(finishingLog));
    }
}