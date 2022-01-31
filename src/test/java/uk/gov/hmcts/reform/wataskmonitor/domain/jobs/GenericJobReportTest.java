package uk.gov.hmcts.reform.wataskmonitor.domain.jobs;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class GenericJobReportTest extends UnitBaseTest {

    @Test
    void isWellImplemented() {

        final Class<?> classUnderTest = GenericJobReport.class;

        assertPojoMethodsFor(classUnderTest)
            .testing(Method.GETTER)
            .testing(Method.CONSTRUCTOR)
            .testing(Method.EQUALS)
            .testing(Method.HASH_CODE)
            .testing(Method.TO_STRING)
            .areWellImplemented();

    }

    @Test
    void should_return_total_number_of_generic_job_report() {
        GenericJobReport genericJobReport = new GenericJobReport(
            3,
            List.of(
                GenericJobOutcome.builder()
                    .taskId(SOME_TASK_ID_1)
                    .processInstanceId(SOME_PROCESS_INSTANCE_ID_1)
                    .successful(true)
                    .jobType("Task Creation")
                    .build(),
                GenericJobOutcome.builder()
                    .taskId(SOME_TASK_ID_2)
                    .processInstanceId(SOME_PROCESS_INSTANCE_ID_2)
                    .successful(false)
                    .jobType("Task Initiation")
                    .build(),
                GenericJobOutcome.builder()
                    .taskId(SOME_TASK_ID_3)
                    .processInstanceId(SOME_PROCESS_INSTANCE_ID_3)
                    .successful(false)
                    .jobType("Task Configuration")
                    .build()
            )
        );

        assertThat(genericJobReport.getTotalNumberOfSuccesses()).isEqualTo(1);
        assertThat(genericJobReport.getTotalNumberOfFailures()).isEqualTo(2);
        assertThat(genericJobReport.getTotalNumberOfTasksProcessed()).isEqualTo(3);
    }

}
