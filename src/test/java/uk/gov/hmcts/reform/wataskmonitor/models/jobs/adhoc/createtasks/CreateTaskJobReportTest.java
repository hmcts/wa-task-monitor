package uk.gov.hmcts.reform.wataskmonitor.models.jobs.adhoc.createtasks;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CreateTaskJobReport;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class CreateTaskJobReportTest {

    @Test
    void isWellImplemented() {

        final Class<?> classUnderTest = CreateTaskJobReport.class;

        assertPojoMethodsFor(classUnderTest)
            .testing(Method.GETTER)
            .testing(Method.CONSTRUCTOR)
            .testing(Method.TO_STRING)
            .areWellImplemented();

    }

}
