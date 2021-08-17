package uk.gov.hmcts.reform.wataskmonitor.domain.idam;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class UserIdamTokenGeneratorInfoTest {

    @Test
    void isWellImplemented() {
        final Class<?> classUnderTest = UserIdamTokenGeneratorInfo.class;
        assertPojoMethodsFor(classUnderTest)
            .testing(Method.GETTER)
            .testing(Method.CONSTRUCTOR)
            .testing(Method.TO_STRING)
            .areWellImplemented();
    }

}
