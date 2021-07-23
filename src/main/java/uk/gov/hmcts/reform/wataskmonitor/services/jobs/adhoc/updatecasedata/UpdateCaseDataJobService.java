package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.updatecasedata;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.JobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchCaseList;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.ElasticSearchRetrieverParameter;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.ResourceEnum;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.retrievecaselist.ElasticSearchCaseRetrieverService;

@Component
public class UpdateCaseDataJobService {

    private final ElasticSearchCaseRetrieverService caseRetrieverService;

    public UpdateCaseDataJobService(ElasticSearchCaseRetrieverService caseRetrieverService) {
        this.caseRetrieverService = caseRetrieverService;
    }

    public JobReport updateCaseData(String serviceToken) {
        ElasticSearchCaseList searchCaseList = caseRetrieverService.retrieveCaseList(
            new ElasticSearchRetrieverParameter(
                serviceToken,
                ResourceEnum.AD_HOC_UPDATE_CASE_CCD_ELASTIC_SEARCH_QUERY
            ));

        return updateCases(searchCaseList);
    }

    private JobReport updateCases(ElasticSearchCaseList searchCaseList) {
        return null;
    }
}
