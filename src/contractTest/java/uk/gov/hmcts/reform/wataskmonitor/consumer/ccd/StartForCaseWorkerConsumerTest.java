package uk.gov.hmcts.reform.wataskmonitor.consumer.ccd;

import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.LambdaDslJsonBody;
import au.com.dius.pact.consumer.dsl.LambdaDslObject;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.wataskmonitor.consumer.ccd.util.CcdConsumerTestBase;

import java.util.Map;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wataskmonitor.consumer.ccd.util.PactDslBuilderForCaseDetailsList.buildStartForCaseWorkerPactDsl;

public class StartForCaseWorkerConsumerTest extends CcdConsumerTestBase {

    public static final String EVENT_ID = "eventId";

    @Override
    public Map<String, Object> setUpStateMapForProviderWithoutCaseData() {
        Map<String, Object> caseDataContentMap = super.setUpStateMapForProviderWithoutCaseData();
        caseDataContentMap.put(EVENT_ID, START_APPEAL);
        return caseDataContentMap;
    }

    @Pact(provider = "ccdDataStoreAPI_Cases", consumer = "wa_task_monitor")
    public RequestResponsePact startForCaseWorker(PactDslWithProvider builder) {
        return builder
            .given("A Start for a Caseworker is requested", setUpStateMapForProviderWithoutCaseData())
            .uponReceiving("A Start for a Caseworker")
            .path(buildPath())
            .method("GET")
            .matchHeader(AUTHORIZATION, AUTH_TOKEN)
            .matchHeader(SERVICE_AUTHORIZATION, SERVICE_AUTH_TOKEN).matchHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .status(200)
            .body(buildStartForCaseWorkerPactDsl(START_APPEAL))
//            .body(LambdaDsl.newJsonBody(((lambdaDslJsonBody) -> {
//                    string
//                }))
            .toPact();
    }

//            .body(String.valueOf(newJsonBody(
//                o -> o.stringType("event_id", START_APPEAL)
//                    .stringType("token", null)
//                    .object("case_details", cd -> {
//                        cd.stringMatcher("jurisdiction", "[/^[A-Za-z_]+$/]+", "IA");
//                        cd.stringMatcher("case_type_id", "[/^[A-Za-z_]+$/]+", "Asylum");
//                        cd.object("case_data", data -> {
//                            data.stringMatcher("isOutOfCountryEnabled", "Yes|No|YES|NO", "No");
//                            data.stringMatcher("appealOutOfCountry", "Yes|No|YES|NO", "No");
//                        });
//                    }).build()



    @Test
    @PactTestFor(pactMethod = "startForCaseWorker", pactVersion = PactSpecVersion.V3)
    public void verifyStartEventForCaseworker() throws JSONException {

        StartEventResponse startEventResponse = coreCaseDataApi.startForCaseworker(
            AUTH_TOKEN,
            SERVICE_AUTH_TOKEN,
            USER_ID,
            "IA",
            "Asylum",
            START_APPEAL);

        assertThat(startEventResponse.getEventId(), equalTo(START_APPEAL));
        assertNotNull(startEventResponse.getCaseDetails());
    }

    private String buildPath() {
        return new StringBuilder()
            .append("/caseworkers/")
            .append(USER_ID)
            .append("/jurisdictions/")
            .append("IA")
            .append("/case-types/")
            .append("Asylum")
            .append("/event-triggers/")
            .append(START_APPEAL)
            .append("/token")
            .toString();
    }

}
