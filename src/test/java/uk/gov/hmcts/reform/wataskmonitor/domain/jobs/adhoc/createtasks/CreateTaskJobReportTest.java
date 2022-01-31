package uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class CreateTaskJobReportTest extends UnitBaseTest {

    @Test
    void isWellImplemented() {

        final Class<?> classUnderTest = CreateTaskJobReport.class;

        assertPojoMethodsFor(classUnderTest)
            .testing(Method.GETTER)
            .testing(Method.CONSTRUCTOR)
            .testing(Method.EQUALS)
            .testing(Method.HASH_CODE)
            .testing(Method.TO_STRING)
            .areWellImplemented();

    }

    @Test
    void should_return_total_number_of_created_cases() {
        CreateTaskJobReport createTaskJobReport = new CreateTaskJobReport(
            3,
            List.of(
                CreateTaskJobOutcome.builder()
                    .caseId(SOME_CASE_ID_1)
                    .taskId(SOME_TASK_ID_1)
                    .processInstanceId(SOME_PROCESS_INSTANCE_ID_1)
                    .created(true)
                    .build(),
                CreateTaskJobOutcome.builder()
                    .caseId(SOME_CASE_ID_2)
                    .taskId(SOME_TASK_ID_2)
                    .processInstanceId(SOME_PROCESS_INSTANCE_ID_2)
                    .created(false)
                    .build(),
                CreateTaskJobOutcome.builder()
                    .caseId(SOME_CASE_ID_3)
                    .taskId(SOME_TASK_ID_3)
                    .processInstanceId(SOME_PROCESS_INSTANCE_ID_3)
                    .created(false)
                    .build()
            )
        );

        assertThat(createTaskJobReport.getTotalNumberOfCreatedTasks()).isEqualTo(1);
        assertThat(createTaskJobReport.getTotalNumberOfNonCreatedTasks()).isEqualTo(2);
        assertThat(createTaskJobReport.getTotalNumberOfCasesProcessed()).isEqualTo(3);
    }

}
