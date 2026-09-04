package uk.gov.hmcts.reform.wataskmonitor.services.jobs.failure.initiation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation.CamundaService;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation.helpers.InitiationHelpers;

import java.time.ZonedDateTime;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.TASK_INITIATION_FAILURES;

@ExtendWith(MockitoExtension.class)
class TaskInitiationFailuresLogServiceTest {

    private static final String SOME_SERVICE_TOKEN = "some service token";

    @Mock
    private CamundaService camundaService;

    private TaskInitiationFailuresLogService taskInitiationFailuresLogService;

    @BeforeEach
    void setUp() {
        taskInitiationFailuresLogService = new TaskInitiationFailuresLogService(camundaService);
    }

    @Test
    void should_return_empty_report_when_there_are_no_failures() {
        GenericJobReport result = taskInitiationFailuresLogService.reportInitiationFailures(
            emptyList(),
            SOME_SERVICE_TOKEN
        );

        assertEquals(new GenericJobReport(0, emptyList()), result);
        verifyNoInteractions(camundaService);
    }

    @Test
    void should_log_failure_details_and_return_successful_outcome() {
        CamundaTask task = createTask();
        when(camundaService.getTaskVariables(SOME_SERVICE_TOKEN, task.getId()))
            .thenReturn(InitiationHelpers.createMockCamundaVariables());

        GenericJobReport result = taskInitiationFailuresLogService.reportInitiationFailures(
            singletonList(task),
            SOME_SERVICE_TOKEN
        );

        verify(camundaService).getTaskVariables(SOME_SERVICE_TOKEN, task.getId());
        assertEquals(expectedReport(task, true), result);
    }

    @Test
    void should_return_unsuccessful_outcome_when_variables_cannot_be_retrieved() {
        CamundaTask task = createTask();
        when(camundaService.getTaskVariables(SOME_SERVICE_TOKEN, task.getId()))
            .thenThrow(new RuntimeException("Camunda unavailable"));

        GenericJobReport result = taskInitiationFailuresLogService.reportInitiationFailures(
            singletonList(task),
            SOME_SERVICE_TOKEN
        );

        assertEquals(expectedReport(task, false), result);
    }

    private CamundaTask createTask() {
        return InitiationHelpers.createMockedCamundaTask(
            ZonedDateTime.now(),
            ZonedDateTime.now().plusDays(1)
        );
    }

    private GenericJobReport expectedReport(CamundaTask task, boolean successful) {
        GenericJobOutcome outcome = GenericJobOutcome.builder()
            .taskId(task.getId())
            .processInstanceId(task.getProcessInstanceId())
            .successful(successful)
            .jobType(TASK_INITIATION_FAILURES.name())
            .build();
        return new GenericJobReport(1, List.of(outcome));
    }
}
