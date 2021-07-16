package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

import static org.assertj.core.api.Assertions.assertThat;
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

    @ParameterizedTest(name = "jobName: {0} expected: {1}")
    @CsvSource({
        "TERMINATION, false",
        "INITIATION, false",
        "CONFIGURATION, false",
        "AD_HOC_DELETE_PROCESS_INSTANCES, true"
    })
    void canRun(JobName jobName, boolean expectedResult) {
        assertThat(deleteProcessInstancesJob.canHandle(jobName)).isEqualTo(expectedResult);
    }

    @Test
    void run() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);

        String someResponse = "{\"id\": \"78e1a849-d9b3-11eb-bb4f-d62f1f620fc5\",\"type\": \"instance-deletion\" }";
        when(deleteProcessInstancesJobService.deleteProcessInstances(SERVICE_TOKEN))
            .thenReturn(someResponse);

        deleteProcessInstancesJob.run();

        verify(authTokenGenerator).generate();
        verify(deleteProcessInstancesJobService).deleteProcessInstances(SERVICE_TOKEN);
    }
}
