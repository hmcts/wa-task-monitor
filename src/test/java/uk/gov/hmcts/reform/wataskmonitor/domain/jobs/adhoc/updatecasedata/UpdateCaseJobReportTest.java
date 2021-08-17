package uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.updatecasedata;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;
import static uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.updatecasedata.UpdateCaseJobOutcome.builder;

class UpdateCaseJobReportTest extends UnitBaseTest {

    @Test
    void isWellImplemented() {
        final Class<?> classUnderTest = UpdateCaseJobReport.class;
        assertPojoMethodsFor(classUnderTest)
            .testing(Method.GETTER)
            .testing(Method.CONSTRUCTOR)
            .testing(Method.EQUALS)
            .testing(Method.HASH_CODE)
            .testing(Method.TO_STRING)
            .areWellImplemented();
    }

    @Test
    void totalNumberOfNonUpdatedCases() {
        UpdateCaseJobReport updateCaseJobReport = new UpdateCaseJobReport(
            2,
            List.of(
                builder()
                    .caseId(SOME_CASE_ID_1)
                    .updated(true)
                    .build(),
                builder()
                    .caseId(SOME_CASE_ID_2)
                    .updated(false)
                    .build()
            )
        );

        assertThat(updateCaseJobReport.getTotalNumberOfNonUpdatedCases()).isEqualTo(1);
    }
}
