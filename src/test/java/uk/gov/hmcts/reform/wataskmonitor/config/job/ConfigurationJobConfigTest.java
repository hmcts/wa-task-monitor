package uk.gov.hmcts.reform.wataskmonitor.config.job;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class ConfigurationJobConfigTest {
    @Test
    void isWellImplemented() {
        final Class<?> classUnderTest = ConfigurationJobConfig.class;
        assertPojoMethodsFor(classUnderTest)
            .testing(Method.GETTER)
            .testing(Method.SETTER)
            .testing(Method.CONSTRUCTOR)
            .areWellImplemented();
    }

}
