package uk.gov.hmcts.reform.wataskmonitor.services.jobs.termination;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TerminationJobTest {

    public static final String SERVICE_TOKEN = "some s2s token";
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private TerminationJobService terminationJobService;
    @InjectMocks
    private TerminationJob terminationJob;

    @ParameterizedTest(name = "jobName: {0} expected: {1}")
    @CsvSource({
        "TERMINATION, true",
        "INITIATION, false",
        "CONFIGURATION, false",
        "AD_HOC_DELETE_PROCESS_INSTANCES, false"
    })
    void canRun(JobName jobName, boolean expectedResult) {
        assertThat(terminationJob.canRun(jobName)).isEqualTo(expectedResult);
    }

    @Test
    void run() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        terminationJob.run();
        verify(authTokenGenerator).generate();
        verify(terminationJobService).terminateTasks(SERVICE_TOKEN);
    }
}
