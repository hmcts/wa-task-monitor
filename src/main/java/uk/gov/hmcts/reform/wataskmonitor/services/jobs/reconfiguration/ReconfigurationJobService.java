package uk.gov.hmcts.reform.wataskmonitor.services.jobs.reconfiguration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskOperationsClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.TaskOperationRequest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.entities.ExecuteReconfigureTaskFilter;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.entities.TaskFilter;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.entities.TaskOperation;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TaskFilterOperator;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TaskOperationName;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class ReconfigurationJobService {

    private static final String RECONFIGURE_REQUEST_TIME = "reconfigure_request_time";
    private final TaskOperationsClient taskOperationsClient;
    private final long reconfigureMaxTimeLimitSeconds;
    private final long reconfigureRetryWindowTimeLimitHours;
    private final long reconfigureRequestTimeHours;

    @Autowired
    public ReconfigurationJobService(TaskOperationsClient taskOperationsClient,
                                     @Value("${job.reconfiguration.reconfigure_request_time_hours}")
                                     long reconfigureRequestTimeHours,
                                     @Value("${job.reconfiguration.reconfiguration_max_time_limit_seconds}")
                                     long reconfigureMaxTimeLimitSeconds,
                                     @Value("${job.reconfiguration.reconfiguration_retry_window_time_hours}")
                                     long reconfigureRetryWindowTimeLimitHours) {
        this.reconfigureRequestTimeHours = reconfigureRequestTimeHours;
        this.taskOperationsClient = taskOperationsClient;
        this.reconfigureMaxTimeLimitSeconds = reconfigureMaxTimeLimitSeconds;
        this.reconfigureRetryWindowTimeLimitHours = reconfigureRetryWindowTimeLimitHours;
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public String reconfigureTask(String serviceToken) {

        OffsetDateTime reconfigureRequestTime = OffsetDateTime.now()
            .minus(Duration.ofHours(reconfigureRequestTimeHours));

        TaskFilter<?> filter = new ExecuteReconfigureTaskFilter(RECONFIGURE_REQUEST_TIME,
            reconfigureRequestTime,
            TaskFilterOperator.AFTER);

        String operationId = UUID.randomUUID().toString();

        TaskOperation operation = TaskOperation.builder()
            .name(TaskOperationName.EXECUTE_RECONFIGURE)
            .runId(operationId)
            .maxTimeLimit(reconfigureMaxTimeLimitSeconds)
            .retryWindowHours(reconfigureRetryWindowTimeLimitHours)
            .build();

        log.debug("reconfigureTask for operation: {}", operation);
        TaskOperationRequest taskOperationRequest = new TaskOperationRequest(operation, List.of(filter));

        taskOperationsClient.executeOperation(serviceToken, taskOperationRequest);
        return operationId;
    }

}
