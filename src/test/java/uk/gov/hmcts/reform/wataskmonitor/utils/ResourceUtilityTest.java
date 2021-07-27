package uk.gov.hmcts.reform.wataskmonitor.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.wataskmonitor.services.ResourceEnum;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ResourceUtilityTest {

    @ParameterizedTest(name = "{0}")
    @EnumSource(ResourceEnum.class)
    void testGetResource(ResourceEnum resourceEnum) {
        assertDoesNotThrow(() -> assertNotNull(
            ResourceUtility.getResource(resourceEnum))
        );
    }
}
