package uk.gov.hmcts.reform.wataskmonitor.consumer.idam;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.wataskmonitor.SpringBootContractBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.clients.IdamWebApi;
import uk.gov.hmcts.reform.wataskmonitor.domain.idam.Token;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@PactTestFor(providerName = "wa_task_monitor", port = "8892")
@ContextConfiguration(classes = {IdamConsumerApplication.class})
public class IdamConsumerTestForPostToken extends SpringBootContractBaseTest {

    @Autowired
    private IdamWebApi idamApi;

    @Pact(provider = "idamApi_oidc", consumer = "wa_task_monitor")
    public V4Pact generatePactFragmentToken(PactDslWithProvider builder) {

        Map<String, String> responseHeaders = ImmutableMap.<String, String>builder()
            .put("Content-Type", APPLICATION_JSON_VALUE)
            .build();

        return builder
            .given("a token is requested")
            .uponReceiving("Provider receives a POST /o/token request from a WA API")
            .path("/o/token")
            .method(HttpMethod.POST.toString())
            .body("redirect_uri=http%3A%2F%2Fwww.dummy-pact-service.com%2Fcallback"
                  + "&client_id=" + PACT_TEST_CLIENT_ID_VALUE
                  + "&grant_type=password"
                  + "&username=" + PACT_TEST_EMAIL_VALUE
                  + "&password=" + PACT_TEST_PASSWORD_VALUE
                  + "&client_secret=" + PACT_TEST_CLIENT_SECRET_VALUE
                  + "&scope=" + PACT_TEST_SCOPES_VALUE,
                "application/x-www-form-urlencoded")
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .headers(responseHeaders)
            .body(createAuthResponse())
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentToken")
    public void verifyIdamUserDetailsRolesPactToken() {

        Map<String, String> tokenRequestMap = buildTokenRequestMap();
        Token token = idamApi.token(tokenRequestMap);
        assertEquals("eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdjEre", token.getAccessToken(), "Token is not expected");
    }

    private Map<String, String> buildTokenRequestMap() {
        return ImmutableMap.<String, String>builder()
            .put("redirect_uri", "http://www.dummy-pact-service.com/callback")
            .put("client_id", PACT_TEST_CLIENT_ID_VALUE)
            .put("client_secret", PACT_TEST_CLIENT_SECRET_VALUE)
            .put("grant_type", "password")
            .put("username", PACT_TEST_EMAIL_VALUE)
            .put("password", PACT_TEST_PASSWORD_VALUE)
            .put("scope", PACT_TEST_SCOPES_VALUE)
            .build();
    }

    private PactDslJsonBody createAuthResponse() {

        return new PactDslJsonBody()
            .stringType("access_token", "eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdjEre")
            .stringType("scope", "openid roles profile");
    }

}
