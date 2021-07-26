package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.updatecasedata;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.wataskmonitor.config.idam.IdamTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.domain.idam.UserInfo;
import uk.gov.hmcts.reform.wataskmonitor.services.UpdateCaseDataService;

@Component
public class ManagementCategoryDataService implements UpdateCaseDataService {

    public static final String EVEN_DESCRIPTION =
        "caseManagementCategory data added by WA-TASK-MONITOR service and AD_HOC_UPDATE_CASE_DATA job";
    public static final String EVENT_SUMMARY = "caseManagementCategory data added by WA-TASK-MONITOR service";
    public static final String EVENT_ID = "editAppealAfterSubmit";
    public static final String CASE_MANAGEMENT_CATEGORY_FIELD_NAME = "caseManagementCategory";

    private final CoreCaseDataApi coreCaseDataApi;
    private final IdamTokenGenerator systemUserIdamToken;

    public ManagementCategoryDataService(CoreCaseDataApi coreCaseDataApi,
                                         IdamTokenGenerator systemUserIdamToken) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.systemUserIdamToken = systemUserIdamToken;
    }

    @Override
    public boolean updateCaseInCcd(String caseId, String serviceToken) {
        String userToken = systemUserIdamToken.generate();
        UserInfo userInfo = systemUserIdamToken.getUserInfo(userToken);

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            userToken,
            serviceToken,
            userInfo.getUid(),
            "IA",
            "Asylum",
            caseId,
            EVENT_ID
        );

        CaseDetails result = coreCaseDataApi.submitEventForCaseWorker(
            userToken,
            serviceToken,
            userInfo.getUid(),
            "IA",
            "Asylum",
            caseId,
            true,
            CaseDataContent.builder()
                .event(Event.builder()
                           .id(EVENT_ID)
                           .description(EVEN_DESCRIPTION)
                           .summary(EVENT_SUMMARY)
                           .build())
                .eventToken(startEventResponse.getToken())
                .build()
        );
        return result.getData().get(CASE_MANAGEMENT_CATEGORY_FIELD_NAME) != null;
    }
}
