package uk.gov.hmcts.reform.wataskmonitor.clients;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.springframework.util.StreamUtils.copyToString;

public class CamundaClientMock {

    public static void setupPostTaskCamundaResponseMock(WireMockServer mockServer) throws IOException {
        mockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/task"))
                               .willReturn(WireMock.aResponse()
                                               .withStatus(HttpStatus.OK_200)
                                               .withBody(
                                                   copyToString(
                                                       CamundaClientMock.class.getClassLoader().getResourceAsStream(
                                                           "post-task-camunda-response.json"),
                                                       Charset.defaultCharset()
                                                   )
                                               )
                               )
        );

    }
}
