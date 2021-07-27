package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.updatecasedata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
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

    @InjectMocks
    private UpdateCaseDataJobService updateCaseDataJobService;
    private ElasticSearchRetrieverParameter expectedElasticSearchParameter;

    @BeforeEach
    void setUp() {
        expectedElasticSearchParameter = new ElasticSearchRetrieverParameter(
            SOME_SERVICE_TOKEN,
            ResourceEnum.AD_HOC_UPDATE_CASE_CCD_ELASTIC_SEARCH_QUERY
        );
        when(caseRetrieverService.retrieveCaseList(expectedElasticSearchParameter))
            .thenReturn(new ElasticSearchCaseList(
                2,
                List.of(new ElasticSearchCase(SOME_CASE_ID), new ElasticSearchCase(SOME_OTHER_CASE_ID))
            ));
    }

    @Test
    void updateCaseData() {
        when(caseManagementDataService.updateCaseInCcd(SOME_CASE_ID, SOME_SERVICE_TOKEN))
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
        verify(caseManagementDataService).updateCaseInCcd(SOME_CASE_ID, SOME_SERVICE_TOKEN);
        verify(caseManagementDataService).updateCaseInCcd(SOME_OTHER_CASE_ID, SOME_SERVICE_TOKEN);
    }

    @Test
    void givenExceptionShouldCatchItAndContinue() {
        when(caseManagementDataService.updateCaseInCcd(SOME_CASE_ID, SOME_SERVICE_TOKEN))
            .thenReturn(true);

        when(caseManagementDataService.updateCaseInCcd(SOME_OTHER_CASE_ID, SOME_SERVICE_TOKEN))
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
        verify(caseManagementDataService).updateCaseInCcd(SOME_CASE_ID, SOME_SERVICE_TOKEN);
        verify(caseManagementDataService).updateCaseInCcd(SOME_OTHER_CASE_ID, SOME_SERVICE_TOKEN);
    }
}