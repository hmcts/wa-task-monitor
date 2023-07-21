package uk.gov.hmcts.reform.wataskmonitor.services.jobs.replication;

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

public class ReplicationCheckJobTest extends UnitBaseTest {

    @Mock
    private ReplicationCheckJobService replicationCheckJobService;

    @InjectMocks
    private ReplicationCheckJob replicationCheckJob;

    @ParameterizedTest(name = "jobName: {0} expected: {1}")
    @CsvSource({
        "TERMINATION, false",
        "INITIATION, false",
        "PERFORM_REPLICATION_CHECK, true",
        "AD_HOC_DELETE_PROCESS_INSTANCES, false"
    })
    void canRun(JobName jobName, boolean expectedResult) {
        assertThat(replicationCheckJob.canRun(jobName)).isEqualTo(expectedResult);
    }

    @Test
    void run() {
        String operationId = "101";
        when(replicationCheckJobService.replicationCheck(SOME_SERVICE_TOKEN))
            .thenReturn(operationId);

        replicationCheckJob.run(SOME_SERVICE_TOKEN);

        verify(replicationCheckJobService).replicationCheck(SOME_SERVICE_TOKEN);
    }
}
