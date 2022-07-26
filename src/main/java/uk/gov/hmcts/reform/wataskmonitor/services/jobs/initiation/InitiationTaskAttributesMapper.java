package uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaVariable;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.TaskAttribute;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.CFTTaskState;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TaskAttributeDefinition;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTime.CAMUNDA_DATA_TIME_FORMATTER;
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
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.TASK_ID;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.TASK_STATE;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.TASK_SYSTEM;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.TASK_TYPE;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.TITLE;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.WARNING_LIST;


@Slf4j
@Service
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class InitiationTaskAttributesMapper {

    private final ObjectMapper objectMapper;

    public InitiationTaskAttributesMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<TaskAttribute> mapTaskAttributes(CamundaTask camundaTask, Map<String, CamundaVariable> variables) {
        // Camunda Attributes
        String name = camundaTask.getName();
        String createdDate = CAMUNDA_DATA_TIME_FORMATTER.format(camundaTask.getCreated());
        String dueDate = CAMUNDA_DATA_TIME_FORMATTER.format(camundaTask.getDue());
        String assignee = camundaTask.getAssignee();
        String description = camundaTask.getDescription();
        // Local Variables
        String type = getVariableValue(variables.get(TASK_TYPE.value()), String.class, null);

        if (type == null) {
            String taskId = getVariableValue(variables.get(TASK_ID.value()), String.class, null);
            /*
             * In some R1 tasks the taskType does not exist which will cause it to fail when attempting to initate
             * a task to allow for R1 to R2 migration we attempt to use the taskId instead who's value should be
             * the same as taskType.
             */
            log.info(
                "Task '{}' did not have a 'taskType' defaulting to 'taskId' with value '{}'",
                camundaTask.getId(), taskId
            );
            type = taskId;
        }

        CFTTaskState taskState = extractTaskState(variables);
        String executionTypeName = getVariableValue(variables.get(EXECUTION_TYPE.value()),
            String.class,
            null);
        String securityClassification = getVariableValue(variables.get(SECURITY_CLASSIFICATION.value()),
            String.class,
            null);
        String taskTitle = getVariableValue(variables.get(TITLE.value()), String.class, null);

        boolean autoAssigned = getVariableValue(variables.get(AUTO_ASSIGNED.value()), Boolean.class, false);
        String taskSystem = getVariableValue(variables.get(TASK_SYSTEM.value()), String.class, null);
        String jurisdiction = getVariableValue(variables.get(JURISDICTION.value()), String.class, null);
        String region = getVariableValue(variables.get(REGION.value()), String.class, null);
        String location = getVariableValue(variables.get(LOCATION.value()), String.class, null);
        String locationName = getVariableValue(variables.get(LOCATION_NAME.value()), String.class, null);
        String caseTypeId = getVariableValue(variables.get(CASE_TYPE_ID.value()), String.class, null);
        String caseId = getVariableValue(variables.get(CASE_ID.value()), String.class, null);
        String caseName = getVariableValue(variables.get(CASE_NAME.value()), String.class, null);
        Boolean hasWarnings = getVariableValue(variables.get(HAS_WARNINGS.value()), Boolean.class, null);
        String warningList = getVariableValue(variables.get(WARNING_LIST.value()), String.class, null);
        String caseManagementCategory = getVariableValue(variables.get(CASE_MANAGEMENT_CATEGORY.value()),
            String.class,
            null);

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
            new TaskAttribute(TaskAttributeDefinition.TASK_EXECUTION_TYPE_NAME, executionTypeName),
            new TaskAttribute(TaskAttributeDefinition.TASK_HAS_WARNINGS, hasWarnings),
            new TaskAttribute(TaskAttributeDefinition.TASK_JURISDICTION, jurisdiction),
            new TaskAttribute(TaskAttributeDefinition.TASK_LOCATION, location),
            new TaskAttribute(TaskAttributeDefinition.TASK_LOCATION_NAME, locationName),
            new TaskAttribute(TaskAttributeDefinition.TASK_NAME, name),
            new TaskAttribute(TaskAttributeDefinition.TASK_WARNINGS, warningList),
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
            new TaskAttribute(TaskAttributeDefinition.TASK_WORK_TYPE, null),
            new TaskAttribute(TaskAttributeDefinition.TASK_NOTES, null)
        );
    }

    public Map<String, Object> mapTaskAttributes(Map<String, CamundaVariable> variables) {
        return variables.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> getCamundaVariableValue(entry.getValue())));

    }

    private Object getCamundaVariableValue(CamundaVariable variable){
        switch (variable.getType()){

            case "String":
                return getVariableValue(variable, String.class, null);
            case "Boolean":
                return getVariableValue(variable, Boolean.class, null);
            case "Integer":
                return getVariableValue(variable, Integer.class, null);
            default:
                return variable.getValue();
        }
    }

    private CFTTaskState extractTaskState(Map<String, CamundaVariable> variables) {
        String taskStateValue = getVariableValue(variables.get(TASK_STATE.value()), String.class, null);
        if (taskStateValue != null) {
            Optional<CFTTaskState> value = CFTTaskState.from(taskStateValue);
            if (value.isPresent()) {
                return value.get();
            } else {
                log.error("could not be mapped");
                throw new IllegalStateException(
                    "taskState " + taskStateValue + " could not be mapped to CFTTaskState enum"
                );
            }
        }
        throw new IllegalStateException("taskState cannot be null");
    }

    private <T> T getVariableValue(CamundaVariable variable, Class<T> type, T defaultValue) {
        Optional<T> value = map(variable, type);
        return value.orElse(defaultValue);
    }

    private <T> Optional<T> map(CamundaVariable variable, Class<T> type) {
        if (variable == null) {
            return Optional.empty();
        }
        T value = objectMapper.convertValue(variable.getValue(), type);

        return Optional.of(value);
    }

}

