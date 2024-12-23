package uk.gov.hmcts.reform.wataskmonitor.services.jobs.replication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskOperationClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.TaskOperationRequest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.entities.TaskOperation;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TaskOperationName;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class ReplicationCheckJobService {

    private final TaskOperationClient taskOperationClient;

    @Autowired
    public ReplicationCheckJobService(TaskOperationClient taskOperationClient) {
        this.taskOperationClient = taskOperationClient;
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public String replicationCheck(String serviceToken) {
        String operationId = UUID.randomUUID().toString();
        TaskOperation operation = new TaskOperation(TaskOperationName.PERFORM_REPLICATION_CHECK,
                                                    operationId);
        log.debug("Send request for operation: {}", operation);
        TaskOperationRequest taskOperationRequest = new TaskOperationRequest(operation, List.of());

        taskOperationClient.executeOperation(serviceToken, taskOperationRequest);
        return operationId;
    }

}
