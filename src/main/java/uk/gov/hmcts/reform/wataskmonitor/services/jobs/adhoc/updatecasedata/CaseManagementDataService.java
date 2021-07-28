package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.updatecasedata;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.wataskmonitor.services.UpdateCaseDataService;

@Component
public class CaseManagementDataService implements UpdateCaseDataService<CaseManagementDataParameter> {

    public static final String EVEN_DESCRIPTION =
        "caseManagementCategory data added by WA-TASK-MONITOR service and AD_HOC_UPDATE_CASE_DATA job";
    public static final String EVENT_SUMMARY = "caseManagementCategory data added by WA-TASK-MONITOR service";
    public static final String EVENT_ID = "adminCaseUpdate";
    public static final String CASE_MANAGEMENT_CATEGORY_FIELD_NAME = "caseManagementCategory";

    private final CoreCaseDataApi coreCaseDataApi;

    public CaseManagementDataService(CoreCaseDataApi coreCaseDataApi) {
        this.coreCaseDataApi = coreCaseDataApi;
    }

    @Override
    public boolean updateCaseInCcd(CaseManagementDataParameter parameter) {
        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            parameter.getUserAuthorization(),
            parameter.getServiceAuthorization(),
            parameter.getUserId(),
            "IA",
            "Asylum",
            parameter.getCaseId(),
            EVENT_ID
        );

        CaseDetails result = coreCaseDataApi.submitEventForCaseWorker(
            parameter.getUserAuthorization(),
            parameter.getServiceAuthorization(),
            parameter.getUserId(),
            "IA",
            "Asylum",
            parameter.getCaseId(),
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
