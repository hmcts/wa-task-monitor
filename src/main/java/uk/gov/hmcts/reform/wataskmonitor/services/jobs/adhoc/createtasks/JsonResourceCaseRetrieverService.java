package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.RetrieveCaseListParam;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.JsonResourceCaseList;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.ResourceEnum;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.retrievecaselist.RetrieveCaseListService;
import uk.gov.hmcts.reform.wataskmonitor.utils.ObjectMapperUtility;
import uk.gov.hmcts.reform.wataskmonitor.utils.ResourceUtility;

@Component
public class JsonResourceCaseRetrieverService implements RetrieveCaseListService<RetrieveCaseListParam> {

    @Override
    public JsonResourceCaseList retrieveCaseList(RetrieveCaseListParam param) {
        return ObjectMapperUtility.stringToObject(
            ResourceUtility.getResource(ResourceEnum.AD_HOC_CREATE_TASKS),
            JsonResourceCaseList.class
        );
    }
}
