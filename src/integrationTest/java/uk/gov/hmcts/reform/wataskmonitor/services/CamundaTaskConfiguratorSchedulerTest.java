package uk.gov.hmcts.reform.wataskmonitor.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.wataskmonitor.schedulers.TaskConfiguratorScheduler;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class CamundaTaskConfiguratorSchedulerTest {

    @MockBean
    private CamundaClient camundaClient;

    @SpyBean
    private TaskConfiguratorScheduler taskConfiguratorScheduler;

    @Test
    void runTaskConfigurator() {
        assertTaskConfiguratorRunsEveryTenSeconds();
    }

    private void assertTaskConfiguratorRunsEveryTenSeconds() {
        await().atMost(12, TimeUnit.SECONDS)
            .untilAsserted(
                () -> {
                    verify(taskConfiguratorScheduler, times(2)).runTaskConfigurator();
                    verify(camundaClient, times(2))
                        .getTasks(anyString(), any(), any(), anyString());
                });
    }
}
