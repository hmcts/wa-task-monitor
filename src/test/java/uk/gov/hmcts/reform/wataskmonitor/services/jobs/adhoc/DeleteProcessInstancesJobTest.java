package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.deleteprocessinstances.DeleteProcessInstancesJob;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.deleteprocessinstances.DeleteProcessInstancesJobService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.AD_HOC_DELETE_PROCESS_INSTANCES;

class DeleteProcessInstancesJobTest extends UnitBaseTest {

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
        assertThat(deleteProcessInstancesJob.canRun(jobName)).isEqualTo(expectedResult);
    }

    @Test
    void run() {
        String someResponse = "{\"id\": \"78e1a849-d9b3-11eb-bb4f-d62f1f620fc5\",\"type\": \"instance-deletion\" }";
        when(deleteProcessInstancesJobService.deleteProcessInstances(SOME_SERVICE_TOKEN))
            .thenReturn(someResponse);

        deleteProcessInstancesJob.run(
            SOME_SERVICE_TOKEN,
            new JobDetails(AD_HOC_DELETE_PROCESS_INSTANCES, "1000")
        );

        verify(deleteProcessInstancesJobService).deleteProcessInstances(SOME_SERVICE_TOKEN);
    }
}
