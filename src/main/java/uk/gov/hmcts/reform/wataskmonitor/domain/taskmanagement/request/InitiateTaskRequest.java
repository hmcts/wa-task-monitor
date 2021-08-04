package uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.InitiateTaskOperation;

import java.util.List;

@EqualsAndHashCode
@ToString
public class InitiateTaskRequest {

    private final InitiateTaskOperation operation;
    private final List<TaskAttribute> taskAttributes;

    @JsonCreator
    public InitiateTaskRequest(InitiateTaskOperation operation, List<TaskAttribute> taskAttributes) {
        this.operation = operation;
        this.taskAttributes = taskAttributes;
    }

    public InitiateTaskOperation getOperation() {
        return operation;
    }

    public List<TaskAttribute> getTaskAttributes() {
        return taskAttributes;
    }
}
