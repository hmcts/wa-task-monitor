package uk.gov.hmcts.reform.wataskmonitor.clients.camunda;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class CamundaWireMockConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer camundaMockServer() {
        return new WireMockServer(9561);
    }
}
