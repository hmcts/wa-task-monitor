package uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaVariable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTime.CAMUNDA_DATA_TIME_FORMATTER;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.ASSIGNEE;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.CREATED;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.DESCRIPTION;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.DUE_DATE;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.PRIORITY_DATE;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.TASK_ID;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.TASK_NAME;
import static uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums.CamundaVariableDefinition.TASK_TYPE;

@Slf4j
@Service
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class InitiationTaskAttributesMapper {

    private final ObjectMapper objectMapper;

    public InitiationTaskAttributesMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> mapTaskAttributes(CamundaTask camundaTask, Map<String, CamundaVariable> variables) {
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

        Map<String, Object> attributes = new ConcurrentHashMap<>();
        attributes.put(TASK_TYPE.value(), type);
        attributes.put(DUE_DATE.value(), CAMUNDA_DATA_TIME_FORMATTER.format(camundaTask.getDue()));
        attributes.put(CREATED.value(), CAMUNDA_DATA_TIME_FORMATTER.format(camundaTask.getCreated()));
        if (camundaTask.getAssignee() != null) {
            attributes.put(ASSIGNEE.value(), camundaTask.getAssignee());
        }
        if (camundaTask.getDescription() != null) {
            attributes.put(DESCRIPTION.value(), camundaTask.getDescription());
        }
        attributes.put(TASK_NAME.value(), camundaTask.getName());
        variables.entrySet().stream()
            .filter(variable -> !variable.getKey().equals(DUE_DATE.value()))
            .filter(variable -> !variable.getKey().equals(ASSIGNEE.value()))
            .filter(variable -> !variable.getKey().equals(PRIORITY_DATE.value()))
            .filter(variable -> !variable.getKey().equals(DESCRIPTION.value()))
            .filter(variable -> !variable.getKey().equals(TASK_NAME.value()))
            .forEach(entry -> attributes.put(entry.getKey(), getCamundaVariableValue(entry.getValue())));
        return attributes;
    }

    private Object getCamundaVariableValue(CamundaVariable variable) {
        switch (variable.getType()) {

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

