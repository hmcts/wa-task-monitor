package uk.gov.hmcts.reform.wataskmonitor.clients.taskconfiguration;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TaskConfigurationWireMockConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer taskConfigurationMockServer() {
        return new WireMockServer(9562);
    }
}
