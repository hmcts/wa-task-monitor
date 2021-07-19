package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CcdClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchCaseList;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchRetrieverParameter;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.ResourceEnum;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.RetrieveCaseListService;
import uk.gov.hmcts.reform.wataskmonitor.utils.ResourceUtility;

@Component
public class ElasticSearchCaseRetrieverService implements RetrieveCaseListService<ElasticSearchRetrieverParameter> {

    private final CcdClient ccdClient;

    public ElasticSearchCaseRetrieverService(CcdClient ccdClient) {
        this.ccdClient = ccdClient;
    }

    @Override
    public ElasticSearchCaseList getCaseList(ElasticSearchRetrieverParameter param) {
        return ccdClient.searchCases(
            "some Bearer token",
            param.getServiceAuthentication(),
            "Asylum",
            ResourceUtility.getResource(ResourceEnum.AD_HOC_CREATE_TASKS_CCD_ELASTIC_SEARCH_QUERY)
        );
    }
}
