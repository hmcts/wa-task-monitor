package uk.gov.hmcts.reform.wataskmonitor.services.jobs.failure.reconfiguration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskOperationClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.TaskOperationRequest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.entities.TaskOperation;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TaskOperationName;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class ReconfigurationFailuresJobService {

    private final TaskOperationClient taskOperationClient;
    private final long reconfigureMaxTimeLimitSeconds;
    private final long reconfigureRetryWindowTimeLimitHours;

    @Autowired
    public ReconfigurationFailuresJobService(TaskOperationClient taskOperationClient,
                                             @Value("${job.reconfiguration.reconfiguration_max_time_limit_seconds}")
                                                 long reconfigureMaxTimeLimitSeconds,
                                             @Value("${job.reconfiguration.reconfiguration_retry_window_time_hours}")
                                                 long reconfigureRetryWindowTimeLimitHours) {
        this.taskOperationClient = taskOperationClient;
        this.reconfigureMaxTimeLimitSeconds = reconfigureMaxTimeLimitSeconds;
        this.reconfigureRetryWindowTimeLimitHours = reconfigureRetryWindowTimeLimitHours;
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public String reconfigureFailuresTask(String serviceToken) {
        String operationId = UUID.randomUUID().toString();
        TaskOperation operation = new TaskOperation(TaskOperationName.EXECUTE_RECONFIGURE_FAILURES,
            operationId, reconfigureMaxTimeLimitSeconds, reconfigureRetryWindowTimeLimitHours);
        log.debug("reconfigureFailuresTask for operation: {}", operation);
        TaskOperationRequest taskOperationRequest = new TaskOperationRequest(operation, List.of());

        taskOperationClient.executeOperation(serviceToken, taskOperationRequest);
        return operationId;
    }

}
