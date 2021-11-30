package uk.gov.hmcts.reform.wataskmonitor.clients.taskconfiguration;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskConfigurationClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "task-management.url=http://localhost:9562"
})
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = {"local"})
@EnableConfigurationProperties
@ContextConfiguration(classes = {TaskConfigurationWireMockConfig.class})
class TaskConfigurationClientTest {

    @Autowired
    private WireMockServer taskConfigurationMockServer;

    @Autowired
    private TaskConfigurationClient taskConfigurationClient;

    @Test
    void givenTaskConfigurationClientIsCalledShouldReturnOk() {
        String taskId = "3ed167c0-c390-11eb-b115-0242ac110012";
        TaskConfigurationClientMock.setupPostTaskConfigurationResponseMock(taskConfigurationMockServer, taskId);

        String actualResponse = taskConfigurationClient.configureTask(
            "some service Bearer token",
            taskId
        );

        assertThat(actualResponse).isEqualTo("OK");
    }
}
