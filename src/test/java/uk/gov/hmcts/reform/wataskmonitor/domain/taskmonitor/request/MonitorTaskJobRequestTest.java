package uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class MonitorTaskJobRequestTest {

    @Test
    void isWellImplemented() {
        final Class<?> classUnderTest = MonitorTaskJobRequest.class;
        assertPojoMethodsFor(classUnderTest)
            .testing(Method.GETTER)
            .testing(Method.CONSTRUCTOR)
            .testing(Method.TO_STRING)
            .areWellImplemented();
    }

}
