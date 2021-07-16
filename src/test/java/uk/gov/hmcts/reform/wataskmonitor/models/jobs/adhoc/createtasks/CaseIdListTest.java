package uk.gov.hmcts.reform.wataskmonitor.models.jobs.adhoc.createtasks;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.ObjectContent;
import pl.pojo.tester.api.assertion.Method;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CaseIdList;

import java.io.IOException;
import java.util.List;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
@JsonTest
class CaseIdListTest {

    @Autowired
    private JacksonTester<CaseIdList> jacksonTester;

    @Test
    void deserializeAsExpected() throws IOException {
        ObjectContent<CaseIdList> caseIdListObjectContent =
            jacksonTester.read("ad-hoc-create-tasks.json");

        caseIdListObjectContent.assertThat()
            .isEqualToComparingFieldByField(new CaseIdList(List.of("1626272789070361", "1626272789070362")));
    }

    @Test
    void isWellImplemented() {

        final Class<?> classUnderTest = CaseIdList.class;

        assertPojoMethodsFor(classUnderTest)
            .testing(Method.GETTER)
            .testing(Method.CONSTRUCTOR)
            .testing(Method.TO_STRING)
            .areWellImplemented();

    }
}
