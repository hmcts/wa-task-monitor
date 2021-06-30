package uk.gov.hmcts.reform.wataskmonitor.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskConfigurationClient;
import uk.gov.hmcts.reform.wataskmonitor.models.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.services.taskconfiguration.TaskConfigurationService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskConfigurationServiceTest {

    public static final String SERVICE_TOKEN = "some service token";
    @Mock
    private TaskConfigurationClient taskConfigurationClient;

    @InjectMocks
    private TaskConfigurationService taskConfigurationService;

    @Captor
    private ArgumentCaptor<String> taskIdCaptor;
    private final CamundaTask task1 = new CamundaTask("some id");
    private final CamundaTask task2 = new CamundaTask("some other id");
    private final List<CamundaTask> camundaTasks = List.of(task1, task2);

    @Test
    void givenUnConfiguredTaskThatCanBeConfiguredShouldConfigureThemSuccessfully() {
        when(taskConfigurationClient.configureTask(
            eq(SERVICE_TOKEN),
            taskIdCaptor.capture()
        )).thenReturn("OK");

        taskConfigurationService.configureTasks(camundaTasks, SERVICE_TOKEN);

        assertThat(taskIdCaptor.getAllValues()).isEqualTo(List.of(task1.getId(), task2.getId()));
        verify(taskConfigurationClient, times(camundaTasks.size()))
            .configureTask(eq(SERVICE_TOKEN), anyString());
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void givenUnConfiguredTaskThatCanNotBeConfiguredShouldCatchException() {
        when(taskConfigurationClient.configureTask(any(), any())).thenThrow(new RuntimeException());
        Assertions.assertDoesNotThrow(() -> taskConfigurationService.configureTasks(camundaTasks, SERVICE_TOKEN));
    }
}
