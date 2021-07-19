package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CreateTaskJobCaseIdList;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.ResourceEnum;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.RetrieveCaseIdListService;
import uk.gov.hmcts.reform.wataskmonitor.utils.ObjectMapperUtility;
import uk.gov.hmcts.reform.wataskmonitor.utils.ResourceUtility;

@Component
public class CreateTaskJobRetrieveCaseIdListService implements RetrieveCaseIdListService {
    @Override
    public CreateTaskJobCaseIdList getCaseIdList() {
        return ObjectMapperUtility
            .stringToObject(ResourceUtility.getResource(ResourceEnum.AD_HOC_CREATE_TASKS), CreateTaskJobCaseIdList.class);
    }
}
