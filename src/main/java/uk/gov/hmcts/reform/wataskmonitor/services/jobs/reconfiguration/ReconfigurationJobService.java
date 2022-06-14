package uk.gov.hmcts.reform.wataskmonitor.services.jobs.reconfiguration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskReconfigurationClient;
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

    private static final String RECONFIGURE_REQUEST_TIME = "reconfigureRequestTime";
    private final TaskReconfigurationClient taskReconfigurationClient;
    private final OffsetDateTime reconfigureRequestTime;

    @Autowired
    public ReconfigurationJobService(TaskReconfigurationClient taskReconfigurationClient,
                                     @Value("${job.reconfiguration.reconfigure_request_time_hours}")
                                         long reconfigureRequestTimeHours) {
        this.taskReconfigurationClient = taskReconfigurationClient;
        this.reconfigureRequestTime = OffsetDateTime.now().minus(Duration.ofHours(reconfigureRequestTimeHours));
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public String reconfigureTask(String serviceToken) {

        TaskFilter<?> filter = new ExecuteReconfigureTaskFilter(RECONFIGURE_REQUEST_TIME,
                                                                reconfigureRequestTime,
                                                                TaskFilterOperator.AFTER);
        String operationId = UUID.randomUUID().toString();
        TaskOperation operation = new TaskOperation(TaskOperationName.EXECUTE_RECONFIGURE,
                                                    operationId);
        TaskOperationRequest taskOperationRequest = new TaskOperationRequest(operation, List.of(filter));

        taskReconfigurationClient.executeReconfigure(serviceToken, taskOperationRequest);
        return operationId;
    }

}
