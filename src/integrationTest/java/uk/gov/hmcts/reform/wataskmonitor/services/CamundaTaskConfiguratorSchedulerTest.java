package uk.gov.hmcts.reform.wataskmonitor.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.models.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.schedulers.TaskConfiguratorScheduler;

import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("integration")
@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.JUnitTestsShouldIncludeAssert"})
class CamundaTaskConfiguratorSchedulerTest {

    @MockBean
    private CamundaService camundaService;
    @MockBean
    private TaskConfigurationService taskConfigurationService;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @SpyBean
    private TaskConfiguratorScheduler taskConfiguratorScheduler;

    @Test
    void assertTaskConfiguratorRunsEveryTenSeconds() {
        when(camundaService.getUnConfiguredTasks(any())).thenReturn(singletonList(new CamundaTask("someId")));
        await().atMost(20, TimeUnit.SECONDS)
            .given()
            .untilAsserted(
                () -> {
                    verify(taskConfiguratorScheduler, times(2)).runTaskConfigurator();
                    verify(camundaService, times(2)).getUnConfiguredTasks(any());
                    verify(taskConfigurationService, times(2)).configureTasks(any(), any());
                });
    }

    @Test
    void assertTaskConfiguratorRunsEveryTenSecondsAndNotCallTaskConfigurationIdEmptyList() {
        when(camundaService.getUnConfiguredTasks(any())).thenReturn(emptyList());
        await().atMost(12, TimeUnit.SECONDS)
            .untilAsserted(
                () -> {
                    verify(taskConfiguratorScheduler, times(2)).runTaskConfigurator();
                    verify(camundaService, times(2)).getUnConfiguredTasks(any());
                    verify(taskConfigurationService, never()).configureTasks(any(), any());
                });
    }
}
