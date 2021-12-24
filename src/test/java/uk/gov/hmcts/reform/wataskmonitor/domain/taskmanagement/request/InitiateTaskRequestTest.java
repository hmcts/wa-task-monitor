package uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.InitiateTaskRequest;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class InitiateTaskRequestTest {

    @Test
    void isWellImplemented() {
        final Class<?> classUnderTest = InitiateTaskRequest.class;

        assertPojoMethodsFor(classUnderTest)
            .testing(Method.GETTER)
            .testing(Method.CONSTRUCTOR)
            .testing(Method.TO_STRING)
            .testing(Method.EQUALS)
            .testing(Method.HASH_CODE)
            .areWellImplemented();
    }

}