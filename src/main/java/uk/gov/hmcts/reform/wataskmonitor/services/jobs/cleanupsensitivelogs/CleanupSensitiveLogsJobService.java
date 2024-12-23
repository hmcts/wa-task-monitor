package uk.gov.hmcts.reform.wataskmonitor.services.jobs.cleanupsensitivelogs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskOperationClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.TaskOperationRequest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.entities.CleanupSensitiveLogsTaskFilter;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.entities.TaskFilter;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.entities.TaskOperation;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TaskFilterOperator;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TaskOperationName;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.response.TaskOperationResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.CLEANUP_SENSITIVE_LOG_ENTRIES;

@Slf4j
@Component
public class CleanupSensitiveLogsJobService {
    private final TaskOperationClient taskOperationClient;

    private static final String CLEAN_UP_START_DATE = "clean_up_start_date";

    @Autowired
    public CleanupSensitiveLogsJobService(TaskOperationClient taskOperationClient) {
        this.taskOperationClient = taskOperationClient;
    }

    public String cleanSensitiveLogs(String serviceToken) {
        OffsetDateTime cleanUpStartDate = OffsetDateTime.now();

        TaskFilter<?> filter = new CleanupSensitiveLogsTaskFilter(
            CLEAN_UP_START_DATE,
            cleanUpStartDate,
            TaskFilterOperator.BEFORE
        );

        String operationId = UUID.randomUUID().toString();

        TaskOperation operation = TaskOperation.builder()
            .name(TaskOperationName.CLEANUP_SENSITIVE_LOG_ENTRIES)
            .runId(operationId)
            .build();

        log.info("{} operation: {}", CLEANUP_SENSITIVE_LOG_ENTRIES, operation);
        TaskOperationRequest taskOperationRequest = new TaskOperationRequest(operation, List.of(filter));

        TaskOperationResponse taskOperationResponse = taskOperationClient
            .executeOperation(serviceToken, taskOperationRequest);

        log.info("{} operation response: {} row(s) deleted", CLEANUP_SENSITIVE_LOG_ENTRIES,
                 taskOperationResponse.getResponseMap().get("deletedRows")
        );
        return operationId;
    }
}
