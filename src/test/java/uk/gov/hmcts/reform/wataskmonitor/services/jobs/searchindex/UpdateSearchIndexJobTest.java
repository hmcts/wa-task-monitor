package uk.gov.hmcts.reform.wataskmonitor.services.jobs.searchindex;

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

class UpdateSearchIndexJobTest extends UnitBaseTest {

    @Mock
    private UpdateSearchIndexJobService updateSearchIndexJobService;

    @InjectMocks
    private UpdateSearchIndexJob updateSearchIndexJob;

    @ParameterizedTest(name = "jobName: {0} expected: {1}")
    @CsvSource({
        "TERMINATION, false",
        "INITIATION, false",
        "UPDATE_SEARCH_INDEX, true",
        "AD_HOC_DELETE_PROCESS_INSTANCES, false"
    })
    void canRun(JobName jobName, boolean expectedResult) {
        assertThat(updateSearchIndexJob.canRun(jobName)).isEqualTo(expectedResult);
    }

    @Test
    void run() {
        String operationId = "101";
        when(updateSearchIndexJobService.updateSearchIndex(SOME_SERVICE_TOKEN))
            .thenReturn(operationId);

        updateSearchIndexJob.run(SOME_SERVICE_TOKEN);

        verify(updateSearchIndexJobService).updateSearchIndex(SOME_SERVICE_TOKEN);
    }
}
