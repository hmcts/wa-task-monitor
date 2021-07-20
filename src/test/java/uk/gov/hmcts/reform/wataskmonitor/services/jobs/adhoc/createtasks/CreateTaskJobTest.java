package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CreateTaskJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CreateTaskJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTaskJobTest {

    public static final String SOME_SERVICE_TOKEN = "some service token";

    @Mock
    private CreateTaskJobService createTaskJobService;

    @InjectMocks
    private CreateTaskJob createTaskJob;

    @ParameterizedTest(name = "jobName: {0} expected: {1}")
    @CsvSource({
        "TERMINATION, false",
        "INITIATION, false",
        "CONFIGURATION, false",
        "AD_HOC_DELETE_PROCESS_INSTANCES, false",
        "AD_HOC_CREATE_TASKS, true"
    })
    void canRun(JobName jobName, boolean expectedResult) {
        assertThat(createTaskJob.canRun(jobName)).isEqualTo(expectedResult);
    }

    @Test
    void run() {
        CreateTaskJobReport someCreateTaskJobReport = new CreateTaskJobReport(
            1,
            List.of(CreateTaskJobOutcome.builder()
                        .created(true)
                        .caseId("some case Id")
                        .taskId("some task id")
                        .build())
        );
        when(createTaskJobService.createTasks(SOME_SERVICE_TOKEN))
            .thenReturn(someCreateTaskJobReport);

        createTaskJob.run(SOME_SERVICE_TOKEN);

        verify(createTaskJobService).createTasks(eq(SOME_SERVICE_TOKEN));

    }

}
