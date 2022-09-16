package uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaVariable;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.TaskAttribute;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.CFTTaskState;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TaskAttributeDefinition;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTime.CAMUNDA_DATA_TIME_FORMATTER;
import static uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation.helpers.InitiationHelpers.createMockCamundaVariables;
import static uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation.helpers.InitiationHelpers.createMockedCamundaTask;

@Slf4j
class InitiationTaskAttributesMapperTest extends UnitBaseTest {

    @InjectMocks
    InitiationTaskAttributesMapper initiationTaskAttributesMapper;

    public static Stream<Arguments> scenarioProvider() {
        Map<String, CamundaVariable> camundaVariables = createMockCamundaVariables();

        Map<String, CamundaVariable> camundaVariablesWithNoAutoAssigned = createMockCamundaVariables();
        camundaVariablesWithNoAutoAssigned.remove("autoAssigned");

        return Stream.of(
            Arguments.of("all vars are present", camundaVariables),
            Arguments.of("autoAssigned is not present", camundaVariablesWithNoAutoAssigned)
        );
    }

    @BeforeEach
    void setUp() {
        initiationTaskAttributesMapper = new InitiationTaskAttributesMapper(new ObjectMapper());
    }

    @ParameterizedTest
    @MethodSource("scenarioProvider")
    void should_map_task_attributes(String scenarioName, Map<String, CamundaVariable> variables) {
        log.info(scenarioName);
        ZonedDateTime createdDate = ZonedDateTime.now();
        ZonedDateTime dueDate = createdDate.plusDays(1);

        CamundaTask camundaTask = createMockedCamundaTask(createdDate, dueDate);

        List<TaskAttribute> actual = initiationTaskAttributesMapper.mapTaskAttributes(camundaTask, variables);
        List<TaskAttribute> expected = getExpectedTaskAttributes(createdDate, dueDate);

        assertThat(actual).hasSameElementsAs(expected);
    }

    @Test
    void should_map_taskId_to_taskType_when_taskType_is_null() {
        Map<String, CamundaVariable> camundaVariablesWithNoTaskType = createMockCamundaVariables();
        camundaVariablesWithNoTaskType.put("taskId", new CamundaVariable("someTaskId", "String"));
        camundaVariablesWithNoTaskType.remove("taskType");
        ZonedDateTime createdDate = ZonedDateTime.now();
        ZonedDateTime dueDate = createdDate.plusDays(1);

        CamundaTask camundaTask = createMockedCamundaTask(createdDate, dueDate);

        List<TaskAttribute> actual = initiationTaskAttributesMapper.mapTaskAttributes(
            camundaTask,
            camundaVariablesWithNoTaskType
        );
        List<TaskAttribute> expected = getExpectedTaskAttributes(createdDate, dueDate, "someTaskId");
        assertThat(actual).hasSameElementsAs(expected);
    }


    private List<TaskAttribute> getExpectedTaskAttributes(ZonedDateTime createdDate,
                                                          ZonedDateTime dueDate,
                                                          String customTaskType) {

        List<TaskAttribute> attributes = new ArrayList<>(getExpectedBaseTaskAttributes(createdDate, dueDate));
        attributes.add(new TaskAttribute(TaskAttributeDefinition.TASK_TYPE, customTaskType));

        return attributes;
    }

    private List<TaskAttribute> getExpectedTaskAttributes(ZonedDateTime createdDate, ZonedDateTime dueDate) {
        List<TaskAttribute> attributes = new ArrayList<>(getExpectedBaseTaskAttributes(createdDate, dueDate));
        attributes.add(new TaskAttribute(TaskAttributeDefinition.TASK_TYPE, "someTaskType"));

        return attributes;
    }

    private List<TaskAttribute> getExpectedBaseTaskAttributes(ZonedDateTime createdDate, ZonedDateTime dueDate) {

        return asList(
            new TaskAttribute(TaskAttributeDefinition.TASK_ASSIGNEE, "someAssignee"),
            new TaskAttribute(TaskAttributeDefinition.TASK_AUTO_ASSIGNED, false),
            new TaskAttribute(TaskAttributeDefinition.TASK_CASE_CATEGORY, "someCaseCategory"),
            new TaskAttribute(TaskAttributeDefinition.TASK_CASE_ID, "00000"),
            new TaskAttribute(TaskAttributeDefinition.TASK_CASE_NAME, "someCaseName"),
            new TaskAttribute(TaskAttributeDefinition.TASK_CASE_TYPE_ID, "someCaseType"),
            new TaskAttribute(TaskAttributeDefinition.TASK_CREATED, CAMUNDA_DATA_TIME_FORMATTER.format(createdDate)),
            new TaskAttribute(TaskAttributeDefinition.TASK_DUE_DATE, CAMUNDA_DATA_TIME_FORMATTER.format(dueDate)),
            new TaskAttribute(TaskAttributeDefinition.TASK_DESCRIPTION, "someCamundaTaskDescription"),
            new TaskAttribute(TaskAttributeDefinition.TASK_EXECUTION_TYPE_NAME, "someExecutionType"),
            new TaskAttribute(TaskAttributeDefinition.TASK_HAS_WARNINGS, true),
            new TaskAttribute(TaskAttributeDefinition.TASK_JURISDICTION, "someJurisdiction"),
            new TaskAttribute(TaskAttributeDefinition.TASK_LOCATION, "someStaffLocationId"),
            new TaskAttribute(TaskAttributeDefinition.TASK_LOCATION_NAME, "someStaffLocationName"),
            new TaskAttribute(TaskAttributeDefinition.TASK_NAME, "someCamundaTaskName"),
            new TaskAttribute(TaskAttributeDefinition.TASK_WARNINGS, "SomeWarningListValue"),
            new TaskAttribute(TaskAttributeDefinition.TASK_REGION, "someRegion"),
            new TaskAttribute(TaskAttributeDefinition.TASK_SECURITY_CLASSIFICATION, "SC"),
            new TaskAttribute(TaskAttributeDefinition.TASK_STATE, CFTTaskState.UNCONFIGURED),
            new TaskAttribute(TaskAttributeDefinition.TASK_SYSTEM, "someTaskSystem"),
            new TaskAttribute(TaskAttributeDefinition.TASK_TITLE, "someTitle"),
            new TaskAttribute(TaskAttributeDefinition.TASK_ROLE_ASSIGNMENT_ID, "someRoleAssignmentId"),
            //Unmapped
            new TaskAttribute(TaskAttributeDefinition.TASK_ASSIGNMENT_EXPIRY, null),
            new TaskAttribute(TaskAttributeDefinition.TASK_BUSINESS_CONTEXT, null),
            new TaskAttribute(TaskAttributeDefinition.TASK_MAJOR_PRIORITY, null),
            new TaskAttribute(TaskAttributeDefinition.TASK_MINOR_PRIORITY, null),
            new TaskAttribute(TaskAttributeDefinition.TASK_ROLES, null),
            new TaskAttribute(TaskAttributeDefinition.TASK_ROLE_CATEGORY, null),
            new TaskAttribute(TaskAttributeDefinition.TASK_REGION_NAME, null),
            new TaskAttribute(TaskAttributeDefinition.TASK_TERMINATION_REASON, null),
            new TaskAttribute(TaskAttributeDefinition.TASK_WORK_TYPE, null),
            new TaskAttribute(TaskAttributeDefinition.TASK_NOTES, null)
        );
    }

}
