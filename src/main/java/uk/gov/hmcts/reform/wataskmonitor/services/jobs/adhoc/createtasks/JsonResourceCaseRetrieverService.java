package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.RetrieveCaseIdListParam;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.JsonResourceCaseList;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.ResourceEnum;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.RetrieveCaseIdListService;
import uk.gov.hmcts.reform.wataskmonitor.utils.ObjectMapperUtility;
import uk.gov.hmcts.reform.wataskmonitor.utils.ResourceUtility;

@Component
public class JsonResourceCaseRetrieverService implements RetrieveCaseIdListService<RetrieveCaseIdListParam> {

    @Override
    public JsonResourceCaseList getCaseIdList(RetrieveCaseIdListParam param) {
        return ObjectMapperUtility.stringToObject(
            ResourceUtility.getResource(ResourceEnum.AD_HOC_CREATE_TASKS),
            JsonResourceCaseList.class
        );
    }
}
