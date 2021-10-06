package uk.gov.hmcts.reform.wataskmonitor.consumer.idam;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.clients.IdamWebApi;

@SpringBootApplication
@EnableFeignClients(clients = {
    IdamWebApi.class
})
public class IdamConsumerApplication {

    @MockBean
    AuthTokenGenerator authTokenGenerator;

    @MockBean
    RestTemplate restTemplate;

}
