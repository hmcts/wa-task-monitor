package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import uk.gov.hmcts.reform.wataskmonitor.clients.CcdClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchCaseList;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchRetrieverParameter;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.RetrieveCaseIdListService;

public class ElasticSearchCaseRetrieverService implements RetrieveCaseIdListService<ElasticSearchRetrieverParameter> {

    private final CcdClient ccdClient;

    public ElasticSearchCaseRetrieverService(CcdClient ccdClient) {
        this.ccdClient = ccdClient;
    }

    @Override
    public ElasticSearchCaseList getCaseIdList(ElasticSearchRetrieverParameter param) {
        return ccdClient.searchCases(
            param.getAuthentication(),
            param.getServiceAuthentication(),
            "Asylum",
            "query"
        );
    }
}
