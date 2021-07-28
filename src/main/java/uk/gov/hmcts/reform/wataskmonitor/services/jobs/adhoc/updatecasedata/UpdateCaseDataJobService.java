package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.updatecasedata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.config.idam.IdamTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.JobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.ElasticSearchRetrieverParameter;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchCaseList;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.updatecasedata.UpdateCaseJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.updatecasedata.UpdateCaseJobReport;
import uk.gov.hmcts.reform.wataskmonitor.services.ResourceEnum;
import uk.gov.hmcts.reform.wataskmonitor.services.retrievecaselist.ElasticSearchCaseRetrieverService;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class UpdateCaseDataJobService {

    private final ElasticSearchCaseRetrieverService caseRetrieverService;
    private final CaseManagementDataService caseManagementDataService;
    private final IdamTokenGenerator systemUserIdamToken;

    public UpdateCaseDataJobService(ElasticSearchCaseRetrieverService caseRetrieverService,
                                    CaseManagementDataService caseManagementDataService,
                                    IdamTokenGenerator systemUserIdamToken) {
        this.caseRetrieverService = caseRetrieverService;
        this.caseManagementDataService = caseManagementDataService;
        this.systemUserIdamToken = systemUserIdamToken;
    }

    public JobReport updateCcdCases(String serviceToken) {
        ElasticSearchCaseList searchCaseList = caseRetrieverService.retrieveCaseList(
            new ElasticSearchRetrieverParameter(
                serviceToken,
                ResourceEnum.AD_HOC_UPDATE_CASE_CCD_ELASTIC_SEARCH_QUERY
            ));
        return updateCasesAndReturnReport(searchCaseList, serviceToken);
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    private JobReport updateCasesAndReturnReport(ElasticSearchCaseList searchCaseList, String serviceToken) {
        log.info("Found {} cases to update...", searchCaseList.getTotal());
        List<UpdateCaseJobOutcome> partialOutcomeList = new ArrayList<>();
        String userAuthorization = systemUserIdamToken.generate();
        String userId = systemUserIdamToken.getUserInfo(userAuthorization).getUid();
        searchCaseList.getCases().forEach(ccdCase -> updateCaseAndReturnReport(
            serviceToken,
            partialOutcomeList,
            userAuthorization,
            userId,
            ccdCase.getId()
        ));
        return new UpdateCaseJobReport(searchCaseList.getTotal(), partialOutcomeList);
    }

    @SuppressWarnings({"PMD.UnnecessaryFullyQualifiedName", "PMD.DataflowAnomalyAnalysis"})
    private void updateCaseAndReturnReport(String serviceToken,
                                           List<UpdateCaseJobOutcome> partialOutcomeList,
                                           String userAuthorization,
                                           String userId,
                                           String caseId) {
        UpdateCaseJobOutcome partialOutcome;
        try {
            boolean updated = caseManagementDataService.updateCaseInCcd(CaseManagementDataParameter.builder()
                                                                            .userAuthorization(userAuthorization)
                                                                            .userId(userId)
                                                                            .serviceAuthorization(serviceToken)
                                                                            .caseId(caseId)
                                                                            .build());
            partialOutcome = UpdateCaseJobOutcome.builder()
                .updated(updated)
                .caseId(caseId)
                .build();
        } catch (Exception e) {
            log.info("Error when updating case in CCD for caseId({}), we carry on...", caseId);
            partialOutcome = UpdateCaseJobOutcome.builder()
                .updated(false)
                .caseId(caseId)
                .build();
        }
        log.info(partialOutcome.toString());
        partialOutcomeList.add(partialOutcome);
    }

}
