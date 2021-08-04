package uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaVariable;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.TaskAttribute;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TaskAttributeDefinition;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation.helpers.InitiationHelpers;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InitiationTaskAttributesMapperTest extends UnitBaseTest {

    @InjectMocks
    InitiationTaskAttributesMapper initiationTaskAttributesMapper;

    @BeforeEach
    void setUp() {
        initiationTaskAttributesMapper = new InitiationTaskAttributesMapper(new ObjectMapper());
    }

    @Test
    void should_map_task_attributes() {

        ZonedDateTime createdDate = ZonedDateTime.now();
        ZonedDateTime dueDate = createdDate.plusDays(1);

        CamundaTask camundaTask = InitiationHelpers.createMockedCamundaTask(createdDate, dueDate);
        Map<String, CamundaVariable> variables = InitiationHelpers.createMockCamundaVariables();

        List<TaskAttribute> actual = initiationTaskAttributesMapper.mapTaskAttributes(camundaTask, variables);
        List<TaskAttribute> expected = getExpectedTaskAttributes(createdDate, dueDate);

        assertEquals(expected, actual);
    }


    private List<TaskAttribute> getExpectedTaskAttributes(ZonedDateTime createdDate, ZonedDateTime dueDate) {

        return asList(
            new TaskAttribute(TaskAttributeDefinition.TASK_ASSIGNEE, "someAssignee"),
            new TaskAttribute(TaskAttributeDefinition.TASK_AUTO_ASSIGNED, false),
            new TaskAttribute(TaskAttributeDefinition.TASK_CASE_CATEGORY, "someCaseCategory"),
            new TaskAttribute(TaskAttributeDefinition.TASK_CASE_ID, "00000"),
            new TaskAttribute(TaskAttributeDefinition.TASK_CASE_NAME, "someCaseName"),
            new TaskAttribute(TaskAttributeDefinition.TASK_CASE_TYPE_ID, "someCaseType"),
            new TaskAttribute(TaskAttributeDefinition.TASK_CREATED, createdDate),
            new TaskAttribute(TaskAttributeDefinition.TASK_DUE_DATE, dueDate),
            new TaskAttribute(TaskAttributeDefinition.TASK_DESCRIPTION, "someCamundaTaskDescription"),
            new TaskAttribute(TaskAttributeDefinition.TASK_EXECUTION_TYPE_CODE, "someExecutionType"),
            new TaskAttribute(TaskAttributeDefinition.TASK_HAS_WARNINGS, true),
            new TaskAttribute(TaskAttributeDefinition.TASK_JURISDICTION, "someJurisdiction"),
            new TaskAttribute(TaskAttributeDefinition.TASK_LOCATION, "someStaffLocationId"),
            new TaskAttribute(TaskAttributeDefinition.TASK_LOCATION_NAME, "someStaffLocationName"),
            new TaskAttribute(TaskAttributeDefinition.TASK_NAME, "someCamundaTaskName"),
            new TaskAttribute(TaskAttributeDefinition.TASK_NOTES, "SomeWarningListValue"),
            new TaskAttribute(TaskAttributeDefinition.TASK_REGION, "someRegion"),
            new TaskAttribute(TaskAttributeDefinition.TASK_SECURITY_CLASSIFICATION, "SC"),
            new TaskAttribute(TaskAttributeDefinition.TASK_STATE, "configured"),
            new TaskAttribute(TaskAttributeDefinition.TASK_SYSTEM, "someTaskSystem"),
            new TaskAttribute(TaskAttributeDefinition.TASK_TITLE, "someTitle"),
            new TaskAttribute(TaskAttributeDefinition.TASK_TYPE, "someTaskType"),
            //Unmapped
            new TaskAttribute(TaskAttributeDefinition.TASK_ASSIGNMENT_EXPIRY, null),
            new TaskAttribute(TaskAttributeDefinition.TASK_BUSINESS_CONTEXT, null),
            new TaskAttribute(TaskAttributeDefinition.TASK_MAJOR_PRIORITY, null),
            new TaskAttribute(TaskAttributeDefinition.TASK_MINOR_PRIORITY, null),
            new TaskAttribute(TaskAttributeDefinition.TASK_ROLES, null),
            new TaskAttribute(TaskAttributeDefinition.TASK_ROLE_CATEGORY, null),
            new TaskAttribute(TaskAttributeDefinition.TASK_REGION_NAME, null),
            new TaskAttribute(TaskAttributeDefinition.TASK_TERMINATION_REASON, null),
            new TaskAttribute(TaskAttributeDefinition.TASK_WORK_TYPE, null)
        );
    }

}
