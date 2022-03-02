package uk.gov.hmcts.reform.wataskmonitor.consumer.ccd;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.wataskmonitor.consumer.ccd.util.CcdConsumerTestBase;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchCaseList;
import uk.gov.hmcts.reform.wataskmonitor.services.ResourceEnum;
import uk.gov.hmcts.reform.wataskmonitor.utils.ResourceUtility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wataskmonitor.consumer.ccd.util.PactDslBuilderForCaseDetailsList.buildSearchResultDsl;

public class SearchCasesConsumerTest  extends CcdConsumerTestBase {

    private static String VALID_QUERY;

    @BeforeAll
    public void setup() {
        VALID_QUERY = ResourceUtility.getResource(ResourceEnum.AD_HOC_CREATE_TASKS_CCD_ELASTIC_SEARCH_QUERY);
        System.out.println(VALID_QUERY);
    }

    @Pact(provider = "ccdDataStoreAPI_searchCases", consumer = "wa_task_monitor")
    public RequestResponsePact searchCases(PactDslWithProvider builder) throws JSONException {
        return builder
            .given("A Search for cases is requested", setUpStateMapForProviderWithCaseData(caseDataContent))
            .uponReceiving("A Search Cases request")
            .path("/searchCases")
            .query("ctid=Asylum")
            .method("POST")
            .body(VALID_QUERY)
            .matchHeader(AUTHORIZATION, AUTH_TOKEN)
            .matchHeader(SERVICE_AUTHORIZATION, SERVICE_AUTH_TOKEN)
            .matchHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .status(HttpStatus.SC_OK)
            .body(buildSearchResultDsl(CASE_ID))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "searchCases")
    public void verifySearchCases() {

        ElasticSearchCaseList searchResult = ccdClient.searchCases(AUTH_TOKEN,
                                                                   SERVICE_AUTH_TOKEN, "Asylum", VALID_QUERY);
        assertEquals(searchResult.getCases().size(), 1);
    }

}
