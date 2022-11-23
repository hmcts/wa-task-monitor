package uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TaskOperationName;

@Schema(
    name = "TaskOperation",
    description = "Allows specifying certain operations on a task"
)
@EqualsAndHashCode
@ToString
public class TaskOperation {

    @Schema(required = true)
    @JsonProperty("name")
    private final TaskOperationName name;

    @JsonProperty("run_id")
    private final String runId;

    @JsonProperty("max_time_limit")
    private final long maxTimeLimit;

    @JsonProperty("retry_window_hours")
    private final long retryWindowHours;

    @JsonCreator
    public TaskOperation(@JsonProperty("name") TaskOperationName name,
                         @JsonProperty("run_id")  String runId,
                         @JsonProperty("max_time_limit") long maxTimeLimit,
                         @JsonProperty("retry_window_hours") long retryWindowHours) {
        this.name = name;
        this.runId = runId;
        this.maxTimeLimit = maxTimeLimit;
        this.retryWindowHours = retryWindowHours;
    }

    public TaskOperationName getName() {
        return name;
    }

    public String getRunId() {
        return runId;
    }

    public long getMaxTimeLimit() {
        return maxTimeLimit;
    }

    public long getRetryWindowHours() {
        return retryWindowHours;
    }
}
