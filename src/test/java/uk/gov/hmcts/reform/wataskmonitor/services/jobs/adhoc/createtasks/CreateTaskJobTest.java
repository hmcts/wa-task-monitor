package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CreateTaskJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CreateTaskJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.AD_HOC_CREATE_TASKS;

class CreateTaskJobTest extends UnitBaseTest {

    @Mock
    private CreateTaskJobService createTaskJobService;

    @InjectMocks
    private CreateTaskJob createTaskJob;

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void canRun() {
        Arrays.stream(JobName.values())
            .forEach(name ->
                         assertThat(createTaskJob.canRun(name)).isEqualTo(AD_HOC_CREATE_TASKS.equals(name)));
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
