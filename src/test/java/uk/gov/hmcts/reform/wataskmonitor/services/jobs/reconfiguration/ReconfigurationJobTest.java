package uk.gov.hmcts.reform.wataskmonitor.services.jobs.reconfiguration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReconfigurationJobTest extends UnitBaseTest {

    @Mock
    private ReconfigurationJobService reconfigurationService;
    @InjectMocks
    private ReconfigurationJob reconfigurationJob;

    @ParameterizedTest(name = "jobName: {0} expected: {1}")
    @CsvSource({
        "TERMINATION, false",
        "INITIATION, false",
        "RECONFIGURATION, true",
        "AD_HOC_DELETE_PROCESS_INSTANCES, false"
    })
    void canRun(JobName jobName, boolean expectedResult) {
        assertThat(reconfigurationJob.canRun(jobName)).isEqualTo(expectedResult);
    }

    @Test
    void run() {
        String operationId = "101";
        when(reconfigurationService.reconfigureTask(SOME_SERVICE_TOKEN))
            .thenReturn(operationId);

        when(reconfigurationService.reconfigureTask(SOME_SERVICE_TOKEN))
            .thenReturn(operationId);

        reconfigurationJob.run(SOME_SERVICE_TOKEN);

        verify(reconfigurationService).reconfigureTask(SOME_SERVICE_TOKEN);
    }
}
