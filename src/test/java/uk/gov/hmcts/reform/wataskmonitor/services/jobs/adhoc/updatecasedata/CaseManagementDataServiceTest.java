package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.updatecasedata;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class CaseManagementDataServiceTest extends UnitBaseTest {

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @InjectMocks
    private CaseManagementDataService caseManagementDataService;

    @ParameterizedTest
    @CsvSource({
        "some data, true",
        ",false"
    })
    @SuppressWarnings("PMD.UnnecessaryFullyQualifiedName")
    void updateCaseInCcd(String someData, boolean expected) {
        when(coreCaseDataApi.startEventForCaseWorker(
            SOME_USER_TOKEN,
            SOME_SERVICE_TOKEN,
            SOME_USER_ID,
            "IA",
            "Asylum",
            SOME_CASE_ID,
            "adminCaseUpdate"
        )).thenReturn(StartEventResponse.builder()
                          .token(SOME_ACCESS_TOKEN)
                          .build());

        when(coreCaseDataApi.submitEventForCaseWorker(
            SOME_USER_TOKEN,
            SOME_SERVICE_TOKEN,
            SOME_USER_ID,
            "IA",
            "Asylum",
            SOME_CASE_ID,
            true,
            CaseDataContent.builder()
                .event(Event.builder()
                           .id(CaseManagementDataService.EVENT_ID)
                           .summary(CaseManagementDataService.EVENT_SUMMARY)
                           .description(CaseManagementDataService.EVEN_DESCRIPTION)
                           .build())
                .eventToken(SOME_ACCESS_TOKEN)
                .build()
        )).thenReturn(CaseDetails.builder()
                          .data(Collections.singletonMap(
                              CaseManagementDataService.CASE_MANAGEMENT_CATEGORY_FIELD_NAME,
                              someData
                          ))
                          .build());

        boolean actual = caseManagementDataService.updateCaseInCcd(CaseManagementDataParameter.builder()
                                                                       .caseId(SOME_CASE_ID)
                                                                       .userAuthorization(SOME_USER_TOKEN)
                                                                       .serviceAuthorization(SOME_SERVICE_TOKEN)
                                                                       .userId(SOME_USER_ID)
                                                                       .build());

        assertThat(actual).isEqualTo(expected);
    }
}
