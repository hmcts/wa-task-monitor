package uk.gov.hmcts.reform.wataskmonitor.services.jobs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetailName;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.DeleteProcessInstancesJob;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.DeleteProcessInstancesJobService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteProcessInstancesJobTest {

    public static final String SERVICE_TOKEN = "some s2s token";
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private DeleteProcessInstancesJobService deleteProcessInstancesJobService;
    @InjectMocks
    private DeleteProcessInstancesJob deleteProcessInstancesJob;

    @ParameterizedTest(name = "jobDetailName: {0} expected: {1}")
    @CsvSource({
        "TERMINATION, false",
        "INITIATION, false",
        "CONFIGURATION, false",
        "AD_HOC, true"
    })
    void canRun(JobDetailName jobDetailName, boolean expectedResult) {
        assertThat(deleteProcessInstancesJob.canRun(jobDetailName)).isEqualTo(expectedResult);
    }

    @Test
    void run() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);

        deleteProcessInstancesJob.run();

        verify(authTokenGenerator).generate();
        verify(deleteProcessInstancesJobService).deleteProcessInstances(eq(SERVICE_TOKEN));
    }
}