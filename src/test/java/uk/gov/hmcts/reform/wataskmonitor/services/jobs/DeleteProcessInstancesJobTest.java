package uk.gov.hmcts.reform.wataskmonitor.services.jobs;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetailName;
import uk.gov.hmcts.reform.wataskmonitor.services.camunda.CamundaService;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DeleteProcessInstancesJobTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CamundaService camundaService;
    @InjectMocks
    private DeleteProcessInstancesJob deleteProcessInstancesJob;

    @ParameterizedTest(name = "jobDetailName: {0}")
    @CsvSource(value = {
        "TERMINATION, false",
        "INITIATION, false",
        "CONFIGURATION, false",
        "AD_HOC, true"
    })
    void canRun(JobDetailName jobDetailName, boolean expectedResult) {
        assertThat(deleteProcessInstancesJob.canRun(jobDetailName)).isEqualTo(expectedResult);
    }
}