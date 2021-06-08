package uk.gov.hmcts.reform.wataskmonitor.exceptions;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class CamundaRequestFailureTest {

    @Test
    void isWellImplemented() {
        final Class<?> classUnderTest = CamundaRequestFailure.class;
        assertPojoMethodsFor(classUnderTest)
            .testing(Method.CONSTRUCTOR)
            .areWellImplemented();
    }

}
