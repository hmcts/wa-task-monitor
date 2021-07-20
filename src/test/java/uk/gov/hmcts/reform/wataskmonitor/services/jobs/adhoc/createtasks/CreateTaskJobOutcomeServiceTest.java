package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CreateTaskJobOutcome;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTaskJobOutcomeServiceTest {

    @Mock
    private CamundaClient camundaClient;

    @InjectMocks
    private CreateTaskJobOutcomeService createTaskJobOutcomeService;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        // to avoid waiting 60 secs for each non-happy path scenario
        changePrivateConstant(createTaskJobOutcomeService, "TIMEOUT", 6);
        changePrivateConstant(createTaskJobOutcomeService, "POLL_INTERVAL", 2);
    }

    @SneakyThrows
    private void changePrivateConstant(Object instance, String constantName, int newValue) {
        Field field = instance.getClass().getDeclaredField(constantName);
        field.setAccessible(true);

        Field modifiers = field.getClass().getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(instance, newValue);
    }

    @Test
    void GivenCamundaClientThrowsExceptionShouldGetJobOutcome() {
        when(camundaClient.getTasksByTaskVariables(
            eq("some service token"),
            eq("caseId_eq_someCaseId,taskType_eq_reviewAppealSkeletonArgument"),
            eq("created"),
            eq("desc")
        )).thenThrow(new RuntimeException("some exception"));

        CreateTaskJobOutcome actual = createTaskJobOutcomeService.getJobOutcome(
            "some service token",
            "someCaseId"
        );

        assertThat(actual).isEqualTo(CreateTaskJobOutcome.builder()
                                         .caseId("someCaseId")
                                         .created(false)
                                         .build());
    }

    @ParameterizedTest
    @MethodSource("scenarioProvider")
    void shouldGetJobOutcome(List<CamundaTask> camundaTaskList, CreateTaskJobOutcome expectedOutcome) {
        when(camundaClient.getTasksByTaskVariables(
            eq("some service token"),
            eq("caseId_eq_someCaseId,taskType_eq_reviewAppealSkeletonArgument"),
            eq("created"),
            eq("desc")
        )).thenReturn(camundaTaskList);

        CreateTaskJobOutcome actual = createTaskJobOutcomeService.getJobOutcome(
            "some service token",
            "someCaseId"
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
                .caseId("someCaseId")
                .created(true)
                .build()
        );

        Arguments taskIsNotFoundScenario = Arguments.of(
            Collections.emptyList(),
            CreateTaskJobOutcome.builder()
                .caseId("someCaseId")
                .created(false)
                .build()
        );

        Arguments camundaTaskListIsNullScenario = Arguments.of(
            null,
            CreateTaskJobOutcome.builder()
                .caseId("someCaseId")
                .created(false)
                .build()
        );

        return Stream.of(
            taskIsCreatedScenario,
            taskIsNotFoundScenario,
            camundaTaskListIsNullScenario
        );
    }
}
