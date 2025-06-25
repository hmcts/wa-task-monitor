package uk.gov.hmcts.reform.wataskmonitor.consumer.idam;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.wataskmonitor.SpringBootContractBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.clients.IdamWebApi;
import uk.gov.hmcts.reform.wataskmonitor.domain.idam.UserInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@PactTestFor(providerName = "wa_task_monitor", port = "8892")
@ContextConfiguration(classes = {IdamConsumerApplication.class})
public class IdamConsumerTestForPostUserInfo extends SpringBootContractBaseTest {

    @Autowired
    private IdamWebApi idamApi;

    @Pact(provider = "idamApi_oidc", consumer = "wa_task_monitor")
    public V4Pact generatePactFragmentUserInfo(PactDslWithProvider builder) {

        return builder
            .given("userinfo is requested")
            .uponReceiving("A request for a UserInfo")
            .path("/o/userinfo")
            .method(HttpMethod.GET.toString())
            .matchHeader(AUTHORIZATION, AUTH_TOKEN)
            .matchHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .status(200)
            .body(createUserDetailsResponse())
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentUserInfo")
    public void verifyIdamUserDetailsRolesPactUserInfo() {
        UserInfo userInfo = idamApi.userInfo(AUTH_TOKEN);
        assertEquals(PACT_TEST_EMAIL_VALUE, userInfo.getEmail(), "User is not Case Officer");
    }

    private PactDslJsonBody createUserDetailsResponse() {
        return new PactDslJsonBody()
            .stringType("uid", "1111-2222-3333-4568")
            .stringValue("sub", "ia-caseofficer@fake.hmcts.net")
            .stringValue("givenName", "Case")
            .stringValue("familyName", "Officer")
            .minArrayLike("roles", 1, PactDslJsonRootValue.stringType("caseworker-ia-legalrep-solicitor"), 1)
            .stringType("IDAM_ADMIN_USER");
    }

}
