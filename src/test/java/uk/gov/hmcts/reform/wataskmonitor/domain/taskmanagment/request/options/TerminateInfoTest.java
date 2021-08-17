package uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagment.request.options;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.options.TerminateInfo;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class TerminateInfoTest {

    @Test
    void isWellImplemented() {
        final Class<?> classUnderTest = TerminateInfo.class;
        assertPojoMethodsFor(classUnderTest)
            .testing(Method.GETTER)
            .testing(Method.CONSTRUCTOR)
            .testing(Method.EQUALS)
            .testing(Method.HASH_CODE)
            .testing(Method.TO_STRING)
            .areWellImplemented();
    }

}
