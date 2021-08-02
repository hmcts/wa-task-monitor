package uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.ElasticSearchRetrieverParameter;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class ElasticSearchRetrieverParameterTest {

    @Test
    void isWellImplemented() {

        final Class<?> classUnderTest = ElasticSearchRetrieverParameter.class;

        assertPojoMethodsFor(classUnderTest)
            .testing(Method.GETTER)
            .testing(Method.CONSTRUCTOR)
            .testing(Method.TO_STRING)
            .areWellImplemented();

    }

}
