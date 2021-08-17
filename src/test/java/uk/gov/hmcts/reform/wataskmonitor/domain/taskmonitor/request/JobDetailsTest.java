package uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class JobDetailsTest {

    @Test
    void isWellImplemented() {
        final Class<?> classUnderTest = JobDetails.class;
        assertPojoMethodsFor(classUnderTest)
            .testing(Method.GETTER)
            .testing(Method.CONSTRUCTOR)
            .testing(Method.TO_STRING)
            .areWellImplemented();
    }

}
