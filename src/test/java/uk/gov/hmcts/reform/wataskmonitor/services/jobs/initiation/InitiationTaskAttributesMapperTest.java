package uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaVariable;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.CFTTaskState;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTime.CAMUNDA_DATA_TIME_FORMATTER;
import static uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation.helpers.InitiationHelpers.createMockCamundaVariables;
import static uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation.helpers.InitiationHelpers.createMockedCamundaTask;

@Slf4j
class InitiationTaskAttributesMapperTest extends UnitBaseTest {

    @InjectMocks
    InitiationTaskAttributesMapper initiationTaskAttributesMapper;

    @BeforeEach
    void setUp() {
        initiationTaskAttributesMapper = new InitiationTaskAttributesMapper(new ObjectMapper());
    }

    @Test
    void should_map_task_attributes_from_camunda_variables() {
        Map<String, CamundaVariable> camundaVariables = createMockCamundaVariables();
        ZonedDateTime createdDate = ZonedDateTime.now();
        ZonedDateTime dueDate = createdDate.plusDays(1);

        CamundaTask camundaTask = createMockedCamundaTask(createdDate, dueDate);

        Map<String, Object> actual = initiationTaskAttributesMapper.mapTaskAttributes(camundaTask, camundaVariables);

        assertEquals(
            camundaVariables.get(CamundaVariableDefinition.AUTO_ASSIGNED.value()).getValue().toString(),
            actual.get(CamundaVariableDefinition.AUTO_ASSIGNED.value()).toString()
        );
        assertEquals(
            camundaVariables.get(CamundaVariableDefinition.HAS_WARNINGS.value()).getValue().toString(),
            actual.get(CamundaVariableDefinition.HAS_WARNINGS.value()).toString()
        );
        assertEquals(
            camundaVariables.get(CamundaVariableDefinition.CASE_ID.value()).getValue().toString(),
            actual.get(CamundaVariableDefinition.CASE_ID.value()).toString()
        );
        assertEquals(
            camundaVariables.get(CamundaVariableDefinition.CASE_NAME.value()).getValue().toString(),
            actual.get(CamundaVariableDefinition.CASE_NAME.value()).toString()
        );
        assertEquals(
            camundaVariables.get(CamundaVariableDefinition.CASE_TYPE_ID.value()).getValue().toString(),
            actual.get(CamundaVariableDefinition.CASE_TYPE_ID.value()).toString()
        );
        assertEquals(
            camundaVariables.get(CamundaVariableDefinition.TASK_STATE.value()).getValue().toString(),
            actual.get(CamundaVariableDefinition.TASK_STATE.value()).toString()
        );
        assertEquals(
            camundaVariables.get(CamundaVariableDefinition.CFT_TASK_STATE.value()).getValue().toString(),
            actual.get(CamundaVariableDefinition.CFT_TASK_STATE.value()).toString()
        );
        assertEquals(
            camundaVariables.get(CamundaVariableDefinition.LOCATION.value()).getValue().toString(),
            actual.get(CamundaVariableDefinition.LOCATION.value()).toString()
        );
        assertEquals(
            camundaVariables.get(CamundaVariableDefinition.LOCATION_NAME.value()).getValue().toString(),
            actual.get(CamundaVariableDefinition.LOCATION_NAME.value()).toString()
        );
        assertEquals(
            camundaVariables.get(CamundaVariableDefinition.SECURITY_CLASSIFICATION.value()).getValue().toString(),
            actual.get(CamundaVariableDefinition.SECURITY_CLASSIFICATION.value()).toString()
        );
        assertEquals(
            camundaVariables.get(CamundaVariableDefinition.TITLE.value()).getValue().toString(),
            actual.get(CamundaVariableDefinition.TITLE.value()).toString()
        );
        assertEquals(
            camundaVariables.get(CamundaVariableDefinition.EXECUTION_TYPE.value()).getValue().toString(),
            actual.get(CamundaVariableDefinition.EXECUTION_TYPE.value()).toString()
        );
        assertEquals(
            camundaVariables.get(CamundaVariableDefinition.TASK_SYSTEM.value()).getValue().toString(),
            actual.get(CamundaVariableDefinition.TASK_SYSTEM.value()).toString()
        );
        assertEquals(
            camundaVariables.get(CamundaVariableDefinition.JURISDICTION.value()).getValue().toString(),
            actual.get(CamundaVariableDefinition.JURISDICTION.value()).toString()
        );
        assertEquals(
            camundaVariables.get(CamundaVariableDefinition.REGION.value()).getValue().toString(),
            actual.get(CamundaVariableDefinition.REGION.value()).toString()
        );
        assertEquals(
            camundaVariables.get(CamundaVariableDefinition.APPEAL_TYPE.value()).getValue().toString(),
            actual.get(CamundaVariableDefinition.APPEAL_TYPE.value()).toString()
        );
        assertEquals(
            camundaVariables.get(CamundaVariableDefinition.CASE_MANAGEMENT_CATEGORY.value()).getValue().toString(),
            actual.get(CamundaVariableDefinition.CASE_MANAGEMENT_CATEGORY.value()).toString()
        );
        assertEquals(
            camundaVariables.get(CamundaVariableDefinition.ROLE_ASSIGNMENT_ID.value()).getValue().toString(),
            actual.get(CamundaVariableDefinition.ROLE_ASSIGNMENT_ID.value()).toString()
        );

    }

    @Test
    void should_map_task_attributes_from_camunda_task() {
        ZonedDateTime createdDate = ZonedDateTime.now();
        ZonedDateTime dueDate = createdDate.plusDays(1);

        CamundaTask camundaTask = new CamundaTask(
            "camundaTaskId",
            "camundaTaskName",
            "camundaProcessInstanceId",
            "camundaAssignee",
            createdDate,
            dueDate,
            "camundaTaskDescription",
            "camundaTaskOwner",
            "camundaTaskFormKey"
        );

        Map<String, CamundaVariable> camundaVariables = new HashMap<>();
        camundaVariables.put(
            CamundaVariableDefinition.TASK_TYPE.value(),
            new CamundaVariable("testTaskType", "String")
        );
        camundaVariables.put(
            CamundaVariableDefinition.TASK_NAME.value(),
            new CamundaVariable("testTask", "String")
        );
        camundaVariables.put(
            CamundaVariableDefinition.ASSIGNEE.value(),
            new CamundaVariable("testAssignee", "String")
        );
        camundaVariables.put(
            CamundaVariableDefinition.DESCRIPTION.value(),
            new CamundaVariable("testDescription", "String")
        );
        camundaVariables.put(
            CamundaVariableDefinition.CREATED.value(),
            new CamundaVariable("20/06/2022", "String")
        );
        camundaVariables.put(
            CamundaVariableDefinition.DUE_DATE.value(),
            new CamundaVariable("20/07/2022", "String")
        );
        camundaVariables.put(
            CamundaVariableDefinition.PRIORITY_DATE.value(),
            new CamundaVariable("20/07/2022", "String")
        );


        Map<String, Object> actual = initiationTaskAttributesMapper.mapTaskAttributes(camundaTask, camundaVariables);

        assertEquals(
            camundaTask.getName(),
            actual.get(CamundaVariableDefinition.TASK_NAME.value()).toString()
        );
        assertEquals(
            camundaTask.getAssignee(),
            actual.get(CamundaVariableDefinition.ASSIGNEE.value()).toString()
        );
        assertEquals(
            CAMUNDA_DATA_TIME_FORMATTER.format(camundaTask.getDue()),
            actual.get(CamundaVariableDefinition.DUE_DATE.value()).toString()
        );
        assertEquals(
            camundaTask.getDescription(),
            actual.get(CamundaVariableDefinition.DESCRIPTION.value()).toString()
        );

        assertNotEquals(
            CAMUNDA_DATA_TIME_FORMATTER.format(camundaTask.getCreated()),
            actual.get(CamundaVariableDefinition.CREATED.value()).toString()
        );
        assertNotEquals(
            camundaVariables.get(CamundaVariableDefinition.TASK_NAME.value()).toString(),
            actual.get(CamundaVariableDefinition.TASK_NAME.value()).toString()
        );
        assertNotEquals(
            camundaVariables.get(CamundaVariableDefinition.ASSIGNEE.value()).toString(),
            actual.get(CamundaVariableDefinition.ASSIGNEE.value()).toString()
        );
        assertNotEquals(
            camundaVariables.get(CamundaVariableDefinition.DUE_DATE.value()).toString(),
            actual.get(CamundaVariableDefinition.DUE_DATE.value()).toString()
        );
        assertNotEquals(
            camundaVariables.get(CamundaVariableDefinition.DESCRIPTION.value()).toString(),
            actual.get(CamundaVariableDefinition.DESCRIPTION.value()).toString()
        );

        assertNull(actual.get(CamundaVariableDefinition.PRIORITY_DATE.value()));
    }

    @Test
    void should_map_taskId_to_taskType_when_taskType_is_null() {
        Map<String, CamundaVariable> camundaVariablesWithNoTaskType = createMockCamundaVariables();
        camundaVariablesWithNoTaskType.put("taskId", new CamundaVariable("someTaskId", "String"));
        camundaVariablesWithNoTaskType.remove("taskType");
        ZonedDateTime createdDate = ZonedDateTime.now();
        ZonedDateTime dueDate = createdDate.plusDays(1);

        CamundaTask camundaTask = new CamundaTask(
            "someCamundaTaskId",
            "someCamundaTaskName",
            "someProcessInstanceId",
            null,
            createdDate,
            dueDate,
            null,
            "someCamundaTaskOwner",
            "someCamundaTaskFormKey"
        );

        Map<String, Object> actual = initiationTaskAttributesMapper.mapTaskAttributes(
            camundaTask,
            camundaVariablesWithNoTaskType
        );
        Map<String, Object> expected = getExpectedTaskAttributes(createdDate, dueDate, "someTaskId", null, null);
        expected.put("taskId", "someTaskId");
        assertThat(actual).isEqualTo(expected);
    }


    private Map<String, Object> getExpectedTaskAttributes(ZonedDateTime createdDate,
                                                          ZonedDateTime dueDate,
                                                          String customTaskType,
                                                          String assignee,
                                                          String description) {

        Map<String, Object> attributes
            = new HashMap<>(getExpectedBaseTaskAttributes(createdDate, dueDate, assignee, description));
        attributes.put(CamundaVariableDefinition.TASK_TYPE.value(), customTaskType);

        return attributes;
    }

    private Map<String, Object> getExpectedBaseTaskAttributes(ZonedDateTime createdDate,
                                                              ZonedDateTime dueDate,
                                                              String assignee,
                                                              String taskDescription) {
        Map<String, Object> attributes = new HashMap<>();

        Optional.ofNullable(assignee).ifPresent(i -> attributes.put(CamundaVariableDefinition.ASSIGNEE.value(), i));

        attributes.put(CamundaVariableDefinition.AUTO_ASSIGNED.value(), false);
        attributes.put(CamundaVariableDefinition.CASE_MANAGEMENT_CATEGORY.value(), "someCaseCategory");
        attributes.put(CamundaVariableDefinition.CASE_ID.value(), "00000");
        attributes.put(CamundaVariableDefinition.CASE_NAME.value(), "someCaseName");
        attributes.put(CamundaVariableDefinition.CASE_TYPE_ID.value(), "someCaseType");
        attributes.put(CamundaVariableDefinition.CREATED.value(), CAMUNDA_DATA_TIME_FORMATTER.format(createdDate));
        attributes.put(CamundaVariableDefinition.DUE_DATE.value(), CAMUNDA_DATA_TIME_FORMATTER.format(dueDate));
        Optional.ofNullable(taskDescription)
            .ifPresent(i -> attributes.put(CamundaVariableDefinition.DESCRIPTION.value(), i));

        attributes.put(CamundaVariableDefinition.EXECUTION_TYPE.value(), "someExecutionType");
        attributes.put(CamundaVariableDefinition.HAS_WARNINGS.value(), true);
        attributes.put(CamundaVariableDefinition.JURISDICTION.value(), "someJurisdiction");
        attributes.put(CamundaVariableDefinition.LOCATION.value(), 1234);
        attributes.put(CamundaVariableDefinition.LOCATION_NAME.value(), "someStaffLocationName");
        attributes.put(CamundaVariableDefinition.TASK_NAME.value(), "someCamundaTaskName");
        attributes.put(CamundaVariableDefinition.WARNING_LIST.value(), "SomeWarningListValue");
        attributes.put(CamundaVariableDefinition.REGION.value(), "someRegion");
        attributes.put(CamundaVariableDefinition.SECURITY_CLASSIFICATION.value(), "SC");
        attributes.put(CamundaVariableDefinition.TASK_STATE.value(), CFTTaskState.UNCONFIGURED.getValue());
        attributes.put(CamundaVariableDefinition.CFT_TASK_STATE.value(), CFTTaskState.UNCONFIGURED.getValue());
        attributes.put(CamundaVariableDefinition.TASK_SYSTEM.value(), "someTaskSystem");
        attributes.put(CamundaVariableDefinition.TITLE.value(), "someTitle");
        attributes.put(CamundaVariableDefinition.APPEAL_TYPE.value(), "someAppealType");
        attributes.put(CamundaVariableDefinition.ROLE_ASSIGNMENT_ID.value(), "someRoleAssignmentId");
        attributes.put("newVariable", "someNewVariable");

        return attributes;

    }
}
