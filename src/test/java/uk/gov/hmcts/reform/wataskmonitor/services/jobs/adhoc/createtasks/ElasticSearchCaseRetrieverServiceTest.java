package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.wataskmonitor.clients.CcdClient;
import uk.gov.hmcts.reform.wataskmonitor.config.idam.IdamTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.ElasticSearchRetrieverParameter;
import uk.gov.hmcts.reform.wataskmonitor.services.ResourceEnum;
import uk.gov.hmcts.reform.wataskmonitor.services.retrievecaselist.ElasticSearchCaseRetrieverService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticSearchCaseRetrieverServiceTest {

    @Mock
    private CcdClient ccdClient;
    @Mock
    private IdamTokenGenerator systemUserIdamToken;

    @InjectMocks
    private ElasticSearchCaseRetrieverService elasticSearchCaseRetrieverService;

    @Test
    void retrieveCaseList() {
        when(systemUserIdamToken.generate()).thenReturn("some user token");

        elasticSearchCaseRetrieverService.retrieveCaseList(
            new ElasticSearchRetrieverParameter(
                "some service token",
                ResourceEnum.AD_HOC_CREATE_TASKS_CCD_ELASTIC_SEARCH_QUERY
            ));

        String expected = "{\n"
                          + "  \"query\": {\n"
                          + "    \"bool\": {\n"
                          + "      \"must\": [\n"
                          + "        {\n"
                          + "          \"match\": {\n"
                          + "            \"state\": \"caseUnderReview\"\n"
                          + "          }\n"
                          + "        }\n"
                          + "      ],\n"
                          + "      \"filter\": [\n"
                          + "        {\n"
                          + "          \"range\": {\n"
                          + "            \"last_state_modified_date\": {\n"
                          + "              \"lte\": \"2021-07-12\",\n"
                          + "              \"gte\": \"2021-05-13\"\n"
                          + "            }\n"
                          + "          }\n"
                          + "        }\n"
                          + "      ]\n"
                          + "    }\n"
                          + "  },\n"
                          + "  \"size\": 276\n"
                          + "}\n";

        Mockito.verify(ccdClient).searchCases(
            eq("some user token"),
            eq("some service token"),
            eq("Asylum"),
            eq(expected)
        );
    }

    @Test
    void retrieveCaseListThrowException() {
        assertThatThrownBy(() -> elasticSearchCaseRetrieverService
            .retrieveCaseList(new ElasticSearchRetrieverParameter(
                null,
                ResourceEnum.AD_HOC_CREATE_TASKS_CCD_ELASTIC_SEARCH_QUERY
            )))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("service token is missing");
    }
}
