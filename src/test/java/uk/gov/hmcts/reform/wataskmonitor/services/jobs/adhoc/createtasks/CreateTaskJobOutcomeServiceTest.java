package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CreateTaskJobOutcome;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CreateTaskJobOutcomeServiceTest extends UnitBaseTest {

    @Mock
    private CamundaClient camundaClient;

    @InjectMocks
    private CreateTaskJobOutcomeService createTaskJobOutcomeService;

    @Test
    void givenCamundaClientThrowsExceptionShouldGetJobOutcome() {
        when(camundaClient.getTasksByTaskVariables(
            eq(SOME_SERVICE_TOKEN),
            eq("caseId_eq_someCaseId,taskType_eq_reviewAppealSkeletonArgument"),
            eq("created"),
            eq("desc")
        )).thenThrow(new RuntimeException("some exception"));

        CreateTaskJobOutcome actual = createTaskJobOutcomeService.getJobOutcome(
            SOME_SERVICE_TOKEN,
            SOME_CASE_ID_CAMEL_CASE
        );

        assertThat(actual).isEqualTo(CreateTaskJobOutcome.builder()
                                         .caseId(SOME_CASE_ID_CAMEL_CASE)
                                         .created(false)
                                         .build());
    }

    @ParameterizedTest
    @MethodSource("scenarioProvider")
    void shouldGetJobOutcome(List<CamundaTask> camundaTaskList, CreateTaskJobOutcome expectedOutcome) {
        when(camundaClient.getTasksByTaskVariables(
            eq(SOME_SERVICE_TOKEN),
            eq("caseId_eq_someCaseId,taskType_eq_reviewAppealSkeletonArgument"),
            eq("created"),
            eq("desc")
        )).thenReturn(camundaTaskList);

        CreateTaskJobOutcome actual = createTaskJobOutcomeService.getJobOutcome(
            SOME_SERVICE_TOKEN,
            SOME_CASE_ID_CAMEL_CASE
        );

        assertThat(actual).isEqualTo(expectedOutcome);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Stream<Arguments> scenarioProvider() {
        Arguments taskIsCreatedScenario = Arguments.of(
            List.of(new CamundaTask(
                "some task id",
                "Review Appeal Skeleton Argument",
                "some process instance id"
            )),
            CreateTaskJobOutcome.builder()
                .taskId("some task id")
                .processInstanceId("some process instance id")
                .caseId(SOME_CASE_ID_CAMEL_CASE)
                .created(true)
                .build()
        );
        Arguments taskNameIsNotEqualScenario = Arguments.of(
            List.of(new CamundaTask(
                "some task id",
                "some other name",
                "some process instance id"
            )),
            CreateTaskJobOutcome.builder()
                .caseId(SOME_CASE_ID_CAMEL_CASE)
                .created(false)
                .build()
        );

        Arguments taskIsNotFoundScenario = Arguments.of(
            Collections.emptyList(),
            CreateTaskJobOutcome.builder()
                .caseId(SOME_CASE_ID_CAMEL_CASE)
                .created(false)
                .build()
        );

        Arguments camundaTaskListIsNullScenario = Arguments.of(
            null,
            CreateTaskJobOutcome.builder()
                .caseId(SOME_CASE_ID_CAMEL_CASE)
                .created(false)
                .build()
        );

        return Stream.of(
            taskIsCreatedScenario,
            taskNameIsNotEqualScenario,
            taskIsNotFoundScenario,
            camundaTaskListIsNullScenario
        );
    }
}
