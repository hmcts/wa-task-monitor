package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.updatecasedata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.config.idam.IdamTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.domain.idam.UserInfo;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.JobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.ElasticSearchRetrieverParameter;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchCase;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchCaseList;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.updatecasedata.UpdateCaseJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.updatecasedata.UpdateCaseJobReport;
import uk.gov.hmcts.reform.wataskmonitor.services.ResourceEnum;
import uk.gov.hmcts.reform.wataskmonitor.services.retrievecaselist.ElasticSearchCaseRetrieverService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateCaseDataJobServiceTest extends UnitBaseTest {

    @Mock
    private ElasticSearchCaseRetrieverService caseRetrieverService;
    @Mock
    private CaseManagementDataService caseManagementDataService;
    @Mock
    private IdamTokenGenerator systemUserIdamToken;
    @InjectMocks
    private UpdateCaseDataJobService updateCaseDataJobService;

    private ElasticSearchRetrieverParameter expectedElasticSearchParameter;
    private CaseManagementDataParameter expectedCaseManagementDataParameter;

    @BeforeEach
    @SuppressWarnings("PMD.UnnecessaryFullyQualifiedName")
    void setUp() {
        expectedElasticSearchParameter = new ElasticSearchRetrieverParameter(
            SOME_SERVICE_TOKEN,
            ResourceEnum.AD_HOC_UPDATE_CASE_CCD_ELASTIC_SEARCH_QUERY
        );

        expectedCaseManagementDataParameter = CaseManagementDataParameter.builder()
            .userId(SOME_USER_ID)
            .serviceAuthorization(SOME_SERVICE_TOKEN)
            .userAuthorization(SOME_USER_TOKEN)
            .caseId(SOME_CASE_ID)
            .build();
        when(caseRetrieverService.retrieveCaseList(expectedElasticSearchParameter))
            .thenReturn(new ElasticSearchCaseList(
                2,
                List.of(new ElasticSearchCase(SOME_CASE_ID), new ElasticSearchCase(SOME_OTHER_CASE_ID))
            ));

        when(systemUserIdamToken.generate()).thenReturn(SOME_USER_TOKEN);
        when(systemUserIdamToken.getUserInfo(SOME_USER_TOKEN)).thenReturn(UserInfo.builder().uid(SOME_USER_ID).build());
    }

    @Test
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    void updateCaseData() {
        when(caseManagementDataService.updateCaseInCcd(expectedCaseManagementDataParameter))
            .thenReturn(true, false);

        JobReport actual = updateCaseDataJobService.updateCcdCases(SOME_SERVICE_TOKEN);

        assertThat(actual).isEqualTo(new UpdateCaseJobReport(
            2,
            List.of(
                UpdateCaseJobOutcome.builder()
                    .caseId(SOME_CASE_ID)
                    .updated(true)
                    .build(),
                UpdateCaseJobOutcome.builder()
                    .caseId(SOME_OTHER_CASE_ID)
                    .updated(false)
                    .build()
            )
        ));

        verify(caseRetrieverService).retrieveCaseList(expectedElasticSearchParameter);
        verify(caseManagementDataService).updateCaseInCcd(expectedCaseManagementDataParameter);
        verify(caseManagementDataService).updateCaseInCcd(expectedCaseManagementDataParameter);
    }

    @Test
    void givenExceptionShouldCatchItAndContinue() {
        when(caseManagementDataService.updateCaseInCcd(expectedCaseManagementDataParameter))
            .thenReturn(true)
            .thenThrow(new RuntimeException("some error"));

        JobReport actual = updateCaseDataJobService.updateCcdCases(SOME_SERVICE_TOKEN);

        assertThat(actual).isEqualTo(new UpdateCaseJobReport(
            2,
            List.of(
                UpdateCaseJobOutcome.builder()
                    .caseId(SOME_CASE_ID)
                    .updated(true)
                    .build(),
                UpdateCaseJobOutcome.builder()
                    .caseId(SOME_OTHER_CASE_ID)
                    .updated(false)
                    .build()
            )
        ));

        verify(caseRetrieverService).retrieveCaseList(expectedElasticSearchParameter);
        verify(caseManagementDataService).updateCaseInCcd(expectedCaseManagementDataParameter);
        verify(caseManagementDataService).updateCaseInCcd(expectedCaseManagementDataParameter);
    }
}
