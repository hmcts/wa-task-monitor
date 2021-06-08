package uk.gov.hmcts.reform.wataskmonitor.clients.taskconfiguration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.eclipse.jetty.http.HttpStatus;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;

public final class TaskConfigurationClientMock {

    private TaskConfigurationClientMock() {
        // HideUtilityClassConstructor
    }

    public static void setupPostTaskConfigurationResponseMock(WireMockServer mockServer, String taskId) {
        mockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/task/" + taskId))
                               .withHeader("ServiceAuthorization", containing("Bearer"))
                               .willReturn(WireMock.aResponse()
                                               .withStatus(HttpStatus.OK_200)
                                               .withHeader("Content-Type", "application/json")
                                               .withBody("OK")
                               )
        );

    }
}
