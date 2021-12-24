package uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TaskAttributeDefinition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class TaskAttributeTest {

    @Test
    void isWellImplemented() {
        final Class<?> classUnderTest = TaskAttribute.class;

        assertPojoMethodsFor(classUnderTest)
            .testing(Method.GETTER)
            .testing(Method.CONSTRUCTOR)
            .testing(Method.TO_STRING)
            .testing(Method.EQUALS)
            .testing(Method.HASH_CODE)
            .areWellImplemented();
    }

    @Test
    void task_attribute_should_return_value_and_name() {

        TaskAttribute taskAttribute = new TaskAttribute(TaskAttributeDefinition.TASK_ASSIGNEE, "someAssignee");
        assertEquals("someAssignee", taskAttribute.getValue());
        assertEquals(TaskAttributeDefinition.TASK_ASSIGNEE, taskAttribute.getName());
        assertEquals("task_assignee",TaskAttributeDefinition.TASK_ASSIGNEE.value());

    }

}