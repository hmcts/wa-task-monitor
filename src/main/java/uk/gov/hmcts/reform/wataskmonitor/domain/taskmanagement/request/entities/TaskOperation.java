package uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TaskOperationName;

@Schema(
    name = "TaskOperation",
    description = "Allows specifying certain operations on a task"
)
@EqualsAndHashCode
@ToString
@Builder
public class TaskOperation {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("name")
    private final TaskOperationName name;

    @JsonProperty("run_id")
    private final String runId;

    @JsonProperty("max_time_limit")
    private long maxTimeLimit;

    @JsonProperty("retry_window_hours")
    private long retryWindowHours;

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

    public TaskOperation(@JsonProperty("name") TaskOperationName name,
                         @JsonProperty("run_id")  String runId) {
        this.name = name;
        this.runId = runId;
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
