package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.updatecasedata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.ElasticSearchRetrieverParameter;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchCaseList;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.ResourceEnum;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.retrievecaselist.ElasticSearchCaseRetrieverService;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCaseDataJobServiceTest {

    public static final String SOME_SERVICE_TOKEN = "some service token";
    @Mock
    private ElasticSearchCaseRetrieverService caseRetrieverService;

    @InjectMocks
    private UpdateCaseDataJobService updateCaseDataJobService;

    @Test
    void updateCaseData() {
        ElasticSearchRetrieverParameter expectedElasticSearchParameter = new ElasticSearchRetrieverParameter(
            SOME_SERVICE_TOKEN,
            ResourceEnum.AD_HOC_UPDATE_CASE_CCD_ELASTIC_SEARCH_QUERY
        );
        when(caseRetrieverService.retrieveCaseList(expectedElasticSearchParameter))
            .thenReturn(new ElasticSearchCaseList(1, null));

        updateCaseDataJobService.updateCaseData(SOME_SERVICE_TOKEN);

        Mockito.verify(caseRetrieverService).retrieveCaseList(expectedElasticSearchParameter);
    }
}
