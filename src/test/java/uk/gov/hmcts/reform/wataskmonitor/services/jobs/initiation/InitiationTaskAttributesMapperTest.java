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
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.CFTTaskState;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

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
        camundaVariablesWithNoAutoAssigned.put("autoAssigned", new CamundaVariable("false", "Boolean"));

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

        Map<String, Object> actual = initiationTaskAttributesMapper.mapTaskAttributes(camundaTask, variables);
        Map<String, Object> expected = getExpectedTaskAttributes(createdDate, dueDate);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void should_map_taskId_to_taskType_when_taskType_is_null() {
        Map<String, CamundaVariable> camundaVariablesWithNoTaskType = createMockCamundaVariables();
        camundaVariablesWithNoTaskType.put("taskId", new CamundaVariable("someTaskId", "String"));
        camundaVariablesWithNoTaskType.remove("taskType");
        ZonedDateTime createdDate = ZonedDateTime.now();
        ZonedDateTime dueDate = createdDate.plusDays(1);

        CamundaTask camundaTask = createMockedCamundaTask(createdDate, dueDate);

        Map<String, Object> actual = initiationTaskAttributesMapper.mapTaskAttributes(
            camundaTask,
            camundaVariablesWithNoTaskType
        );
        Map<String, Object> expected = getExpectedTaskAttributes(createdDate, dueDate, "someTaskId");
        expected.put("taskId", "someTaskId");
        assertThat(actual).isEqualTo(expected);
    }


    private Map<String, Object> getExpectedTaskAttributes(ZonedDateTime createdDate,
                                                          ZonedDateTime dueDate,
                                                          String customTaskType) {

        Map<String, Object> attributes = new HashMap<>(getExpectedBaseTaskAttributes(createdDate, dueDate));
        attributes.put(CamundaVariableDefinition.TASK_TYPE.value(), customTaskType);

        return attributes;
    }

    private Map<String, Object> getExpectedTaskAttributes(ZonedDateTime createdDate, ZonedDateTime dueDate) {
        Map<String, Object> attributes = new HashMap<>(getExpectedBaseTaskAttributes(createdDate, dueDate));
        attributes.put(CamundaVariableDefinition.TASK_TYPE.value(), "someTaskType");

        return attributes;
    }

    private Map<String, Object> getExpectedBaseTaskAttributes(ZonedDateTime createdDate, ZonedDateTime dueDate) {
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(CamundaVariableDefinition.ASSIGNEE.value(), "someAssignee");
        attributes.put(CamundaVariableDefinition.AUTO_ASSIGNED.value(), false);
        attributes.put(CamundaVariableDefinition.CASE_MANAGEMENT_CATEGORY.value(), "someCaseCategory");
        attributes.put(CamundaVariableDefinition.CASE_ID.value(), "00000");
        attributes.put(CamundaVariableDefinition.CASE_NAME.value(), "someCaseName");
        attributes.put(CamundaVariableDefinition.CASE_TYPE_ID.value(), "someCaseType");
        attributes.put(CamundaVariableDefinition.CREATED.value(), CAMUNDA_DATA_TIME_FORMATTER.format(createdDate));
        attributes.put(CamundaVariableDefinition.DUE_DATE.value(), CAMUNDA_DATA_TIME_FORMATTER.format(dueDate));
        attributes.put(CamundaVariableDefinition.DESCRIPTION.value(), "someCamundaTaskDescription");
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
