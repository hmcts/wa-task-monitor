package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.updatecasedata;

import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.wataskmonitor.config.idam.IdamTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.domain.idam.UserInfo;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ManagementCategoryDataServiceTest extends UnitBaseTest {

    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private IdamTokenGenerator idamTokenGenerator;

    @InjectMocks
    private ManagementCategoryDataService managementCategoryDataService;

    @BeforeEach
    void setUp() {
        when(idamTokenGenerator.generate()).thenReturn(SOME_USER_TOKEN);
        when(idamTokenGenerator.getUserInfo(SOME_USER_TOKEN))
            .thenReturn(UserInfo.builder()
                            .uid(SOME_USER_ID)
                            .build());
    }

    @ParameterizedTest
    @CsvSource({
        "some data, true",
        ",false"
    })
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
                           .id(ManagementCategoryDataService.EVENT_ID)
                           .summary(ManagementCategoryDataService.EVENT_SUMMARY)
                           .description(ManagementCategoryDataService.EVEN_DESCRIPTION)
                           .build())
                .eventToken(SOME_ACCESS_TOKEN)
                .build()
        )).thenReturn(CaseDetails.builder()
                          .data(Collections.singletonMap(
                              ManagementCategoryDataService.CASE_MANAGEMENT_CATEGORY_FIELD_NAME,
                              someData
                          ))
                          .build());

        boolean actual = managementCategoryDataService.updateCaseInCcd(SOME_CASE_ID, SOME_SERVICE_TOKEN);

        assertThat(actual).isEqualTo(expected);
    }
}
