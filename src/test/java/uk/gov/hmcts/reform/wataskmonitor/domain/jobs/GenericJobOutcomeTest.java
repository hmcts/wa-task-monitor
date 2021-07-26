package uk.gov.hmcts.reform.wataskmonitor.domain.jobs;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class GenericJobOutcomeTest {
    @Test
    void isWellImplemented() {

        final Class<?> classUnderTest = GenericJobOutcome.class;

        assertPojoMethodsFor(classUnderTest)
            .testing(Method.GETTER)
            .testing(Method.CONSTRUCTOR)
            .testing(Method.TO_STRING)
            .areWellImplemented();

    }
}
