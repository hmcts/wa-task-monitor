package uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskManagementClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation.helpers.InitiationHelpers;

import java.time.ZonedDateTime;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class InitiationServiceTest extends UnitBaseTest {

    private static final String JOB_TYPE = "Task Initiation";

    @Mock
    private CamundaService camundaService;
    @Mock
    private TaskManagementClient taskManagementClient;

    private InitiationService initiationService;

    @BeforeEach
    void setUp() {
        InitiationTaskAttributesMapper mapper = new InitiationTaskAttributesMapper(new ObjectMapper());
        initiationService = new InitiationService(camundaService, taskManagementClient, mapper);
    }

    @Test
    void should_return_empty_report_when_there_are_no_tasks() {
        GenericJobReport result = initiationService.initiateTasks(
            emptyList(),
            SOME_SERVICE_TOKEN,
            JOB_TYPE
        );

        assertEquals(new GenericJobReport(0, emptyList()), result);
        verifyNoInteractions(camundaService, taskManagementClient);
    }

    @Test
    void should_initiate_task_and_return_successful_outcome() {
        CamundaTask task = createTask();
        when(camundaService.getTaskVariables(SOME_SERVICE_TOKEN, task.getId()))
            .thenReturn(InitiationHelpers.createMockCamundaVariables());

        GenericJobReport result = initiationService.initiateTasks(
            singletonList(task),
            SOME_SERVICE_TOKEN,
            JOB_TYPE
        );

        verify(taskManagementClient).initiateTask(
            eq(SOME_SERVICE_TOKEN),
            eq(task.getId()),
            any()
        );
        assertEquals(expectedReport(task, true), result);
    }

    @Test
    void should_return_unsuccessful_outcome_when_task_management_fails() {
        CamundaTask task = createTask();
        when(camundaService.getTaskVariables(SOME_SERVICE_TOKEN, task.getId()))
            .thenReturn(InitiationHelpers.createMockCamundaVariables());
        doThrow(new RuntimeException("Task Management unavailable"))
            .when(taskManagementClient)
            .initiateTask(eq(SOME_SERVICE_TOKEN), eq(task.getId()), any());

        GenericJobReport result = initiationService.initiateTasks(
            singletonList(task),
            SOME_SERVICE_TOKEN,
            JOB_TYPE
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
            .jobType(JOB_TYPE)
            .build();
        return new GenericJobReport(1, List.of(outcome));
    }
}
