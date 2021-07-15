package uk.gov.hmcts.reform.wataskmonitor.clients.camunda;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "camunda.url=http://localhost:9561",
    "task.configurator.scheduling.enable=false"
})
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = {"local"})
@EnableConfigurationProperties
@ContextConfiguration(classes = {CamundaWireMockConfig.class})
class CamundaClientTest {

    @Autowired
    private WireMockServer camundaMockServer;

    @Autowired
    private CamundaClient camundaClient;

    @Test
    void givenCamundaClientIsCalledShouldReturnTasks() throws IOException {
        CamundaClientMock.setupPostTaskCamundaResponseMock(
            camundaMockServer,
            "post-task-camunda-response.json"
        );

        List<CamundaTask> camundaTasks = camundaClient.getTasks(
            "some service Bearer token",
            "0",
            "1000",
            ""
        );

        assertThat(camundaTasks).isEqualTo(Arrays.asList(
            new CamundaTask("090e80f0-c3be-11eb-a06f-164a82de09f9"),
            new CamundaTask("21827953-c3c3-11eb-adeb-3a61f2fe2b47")
        ));

    }
}
