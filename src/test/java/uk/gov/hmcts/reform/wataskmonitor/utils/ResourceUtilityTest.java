package uk.gov.hmcts.reform.wataskmonitor.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.RequestsEnum;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ResourceUtilityTest {

    @ParameterizedTest(name = "{0}")
    @EnumSource(RequestsEnum.class)
    void testGetResource(RequestsEnum requestsEnum) {
        assertDoesNotThrow(() -> assertNotNull(
            ResourceUtility.getResource(requestsEnum.getRequestParameterBody()))
        );
    }
}
