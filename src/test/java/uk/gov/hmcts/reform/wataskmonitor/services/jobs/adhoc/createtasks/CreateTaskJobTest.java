package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetailName;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CreateTaskJobTest {

    @InjectMocks
    private CreateTaskJob createTaskJob;

    @ParameterizedTest(name = "jobDetailName: {0} expected: {1}")
    @CsvSource({
        "TERMINATION, false",
        "INITIATION, false",
        "CONFIGURATION, false",
        "AD_HOC_DELETE_PROCESS_INSTANCES, false",
        "AD_HOC_CREATE_TASKS, true"
    })
    void canRun(JobDetailName jobDetailName, boolean expectedResult) {
        assertThat(createTaskJob.canRun(jobDetailName)).isEqualTo(expectedResult);
    }
}