package uk.gov.hmcts.reform.wataskmonitor.clients.camunda;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.Charset;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static org.springframework.util.StreamUtils.copyToString;
import static uk.gov.hmcts.reform.wataskmonitor.config.SecurityConfiguration.SERVICE_AUTHORIZATION;

public final class CamundaClientMock {

    private CamundaClientMock() {
        // HideUtilityClassConstructor
    }

    public static void setupPostTaskCamundaResponseMock(WireMockServer mockServer, String expectedResponse)
        throws IOException {
        mockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/task?firstResult=0&maxResults=100"))
            .withHeader(SERVICE_AUTHORIZATION, containing("Bearer"))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK_200)
                .withHeader("Content-Type", "application/json")
                .withBody(
                    copyToString(
                        Thread.currentThread().getContextClassLoader()
                            .getResourceAsStream(expectedResponse),
                        Charset.defaultCharset()
                    )
                )
            )
        );

    }
}
