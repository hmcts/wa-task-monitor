package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.wataskmonitor.clients.CcdClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchRetrieverParameter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class ElasticSearchCaseRetrieverServiceTest {

    @Mock
    private CcdClient ccdClient;

    @InjectMocks
    private ElasticSearchCaseRetrieverService elasticSearchCaseRetrieverService;

    @Test
    void retrieveCaseList() {
        elasticSearchCaseRetrieverService.retrieveCaseList(
            new ElasticSearchRetrieverParameter(null));

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
                          + "  \"size\": 10\n"
                          + "}\n";

        Mockito.verify(ccdClient).searchCases(
            eq("some Bearer token"),
            eq("some service token"),
            eq("Asylum"),
            eq(expected)
        );
    }

    @Test
    void retrieveCaseListThrowException() {
        assertThatThrownBy(() -> elasticSearchCaseRetrieverService
            .retrieveCaseList(new ElasticSearchRetrieverParameter(null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("service token is missing");
    }
}
