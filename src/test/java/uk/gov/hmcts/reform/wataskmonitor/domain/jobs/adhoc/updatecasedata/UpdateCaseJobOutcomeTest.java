package uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.updatecasedata;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class UpdateCaseJobOutcomeTest {

    @Test
    void isWellImplemented() {

        final Class<?> classUnderTest = UpdateCaseJobOutcome.class;

        assertPojoMethodsFor(classUnderTest)
            .testing(Method.GETTER)
            .testing(Method.CONSTRUCTOR)
            .testing(Method.EQUALS)
            .testing(Method.HASH_CODE)
            .testing(Method.TO_STRING)
            .areWellImplemented();

    }

}
