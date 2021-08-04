package uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaVariable;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.TaskAttribute;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TaskAttributeDefinition;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.AUTO_ASSIGNED;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.CASE_ID;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.CASE_MANAGEMENT_CATEGORY;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.CASE_NAME;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.CASE_TYPE_ID;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.EXECUTION_TYPE;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.HAS_WARNINGS;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.JURISDICTION;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.LOCATION;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.LOCATION_NAME;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.REGION;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.SECURITY_CLASSIFICATION;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.TASK_STATE;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.TASK_SYSTEM;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.TASK_TYPE;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.TITLE;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.WARNING_LIST;


@Service
public class InitiationTaskAttributesMapper {

    private final ObjectMapper objectMapper;

    public InitiationTaskAttributesMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<TaskAttribute> mapTaskAttributes(CamundaTask camundaTask, Map<String, CamundaVariable> variables) {
        // Camunda Attributes
        String name = camundaTask.getName();
        ZonedDateTime createdDate = camundaTask.getCreated();
        ZonedDateTime dueDate = camundaTask.getDue();
        String assignee = camundaTask.getAssignee();
        String description = camundaTask.getDescription();
        // Local Variables
        String type = getVariableValue(variables.get(TASK_TYPE.value()), String.class);
        String taskState = getVariableValue(variables.get(TASK_STATE.value()), String.class);
        String securityClassification = getVariableValue(variables.get(SECURITY_CLASSIFICATION.value()), String.class);
        String taskTitle = getVariableValue(variables.get(TITLE.value()), String.class);
        String executionType = getVariableValue(variables.get(EXECUTION_TYPE.value()), String.class);
        boolean autoAssigned = getVariableValue(variables.get(AUTO_ASSIGNED.value()), Boolean.class);
        String taskSystem = getVariableValue(variables.get(TASK_SYSTEM.value()), String.class);
        String jurisdiction = getVariableValue(variables.get(JURISDICTION.value()), String.class);
        String region = getVariableValue(variables.get(REGION.value()), String.class);
        String location = getVariableValue(variables.get(LOCATION.value()), String.class);
        String locationName = getVariableValue(variables.get(LOCATION_NAME.value()), String.class);
        String caseTypeId = getVariableValue(variables.get(CASE_TYPE_ID.value()), String.class);
        String caseId = getVariableValue(variables.get(CASE_ID.value()), String.class);
        String caseName = getVariableValue(variables.get(CASE_NAME.value()), String.class);
        Boolean hasWarnings = getVariableValue(variables.get(HAS_WARNINGS.value()), Boolean.class);
        String warningList = getVariableValue(variables.get(WARNING_LIST.value()), String.class);
        String caseManagementCategory = getVariableValue(variables.get(CASE_MANAGEMENT_CATEGORY.value()), String.class);

        return asList(
            new TaskAttribute(TaskAttributeDefinition.TASK_ASSIGNEE, assignee),
            new TaskAttribute(TaskAttributeDefinition.TASK_AUTO_ASSIGNED, autoAssigned),
            new TaskAttribute(TaskAttributeDefinition.TASK_CASE_CATEGORY, caseManagementCategory),
            new TaskAttribute(TaskAttributeDefinition.TASK_CASE_ID, caseId),
            new TaskAttribute(TaskAttributeDefinition.TASK_CASE_NAME, caseName),
            new TaskAttribute(TaskAttributeDefinition.TASK_CASE_TYPE_ID, caseTypeId),
            new TaskAttribute(TaskAttributeDefinition.TASK_CREATED, createdDate),
            new TaskAttribute(TaskAttributeDefinition.TASK_DUE_DATE, dueDate),
            new TaskAttribute(TaskAttributeDefinition.TASK_DESCRIPTION, description),
            new TaskAttribute(TaskAttributeDefinition.TASK_EXECUTION_TYPE_CODE, executionType),
            new TaskAttribute(TaskAttributeDefinition.TASK_HAS_WARNINGS, hasWarnings),
            new TaskAttribute(TaskAttributeDefinition.TASK_JURISDICTION, jurisdiction),
            new TaskAttribute(TaskAttributeDefinition.TASK_LOCATION, location),
            new TaskAttribute(TaskAttributeDefinition.TASK_LOCATION_NAME, locationName),
            new TaskAttribute(TaskAttributeDefinition.TASK_NAME, name),
            new TaskAttribute(TaskAttributeDefinition.TASK_NOTES, warningList),
            new TaskAttribute(TaskAttributeDefinition.TASK_REGION, region),
            new TaskAttribute(TaskAttributeDefinition.TASK_SECURITY_CLASSIFICATION, securityClassification),
            new TaskAttribute(TaskAttributeDefinition.TASK_STATE, taskState),
            new TaskAttribute(TaskAttributeDefinition.TASK_SYSTEM, taskSystem),
            new TaskAttribute(TaskAttributeDefinition.TASK_TITLE, taskTitle),
            new TaskAttribute(TaskAttributeDefinition.TASK_TYPE, type),
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

    public <T> Optional<T> read(CamundaVariable variable, Class<T> type) {
        return map(variable, type);
    }

    private <T> T getVariableValue(CamundaVariable variable, Class<T> type) {
        Optional<T> value = read(variable, type);
        return value.orElse(null);
    }

    private <T> Optional<T> map(CamundaVariable variable, Class<T> type) {

        if (variable == null) {
            return Optional.empty();
        }
        T value = objectMapper.convertValue(variable.getValue(), type);

        return Optional.of(value);
    }

}

