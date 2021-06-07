package uk.gov.hmcts.reform.wataskmonitor.clients;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@SpringBootTest(properties = {"camunda.url=http://localhost:9561"})
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = {"local"})
@EnableConfigurationProperties
@ContextConfiguration(classes = {WireMockConfig.class})
class CamundaClientTest {

    @Autowired
    private WireMockServer mockCamundaApi;

    @Autowired
    private CamundaClient camundaClient;

    @Test
    void getTasks() throws IOException {
        CamundaClientMock.setupPostTaskCamundaResponseMock(mockCamundaApi);

        List<String> tasks = camundaClient.getTasks("s2s token", null, null, Map.of());

        System.out.println(tasks);

    }
}
