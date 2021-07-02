package uk.gov.hmcts.reform.wataskmonitor.services.jobs;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.wataskmonitor.services.utilities.ResourceUtility;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ResourceUtilityTest {

    @ParameterizedTest(name = "{0}")
    @EnumSource(RequestParameterEnum.class)
    void testGetResource(RequestParameterEnum requestParameterEnum) {
        assertDoesNotThrow(() ->
            assertNotNull(ResourceUtility.getResource(requestParameterEnum.getRequestParameterBody())));
    }
}