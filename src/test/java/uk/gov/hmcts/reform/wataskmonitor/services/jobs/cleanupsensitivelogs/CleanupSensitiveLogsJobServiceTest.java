package uk.gov.hmcts.reform.wataskmonitor.services.jobs.cleanupsensitivelogs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskOperationClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.TaskOperationRequest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.response.TaskOperationResponse;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.CLEANUP_SENSITIVE_LOG_ENTRIES;

@ExtendWith(OutputCaptureExtension.class)
class CleanupSensitiveLogsJobServiceTest extends UnitBaseTest {

    private static final String CLEAN_UP_START_DATE = "clean_up_start_date";
    @Mock
    private TaskOperationClient taskOperationClient;
    private CleanupSensitiveLogsJobService cleanupSensitiveLogsJobService;

    @BeforeEach
    void setUp() {
        cleanupSensitiveLogsJobService = new CleanupSensitiveLogsJobService(taskOperationClient);
    }

    @Test
    void clean(CapturedOutput output) {

        TaskOperationResponse taskOperationResponse = new TaskOperationResponse(Map.of("deletedRows", 1));


        when(taskOperationClient.executeOperation(anyString(), any(TaskOperationRequest.class)))
            .thenReturn(taskOperationResponse);

        String operationId = cleanupSensitiveLogsJobService.cleanSensitiveLogs(SOME_SERVICE_TOKEN);
        assertNotNull(operationId);

        verify(taskOperationClient).executeOperation(anyString(), any(TaskOperationRequest.class));

        String requestLog = String.format("%s operation: ", CLEANUP_SENSITIVE_LOG_ENTRIES);
        assertThat(output.getOut().contains(requestLog));

        String responseLog = String.format("%s operation response: %s row(s) deleted",
                                           CLEANUP_SENSITIVE_LOG_ENTRIES,
                                           taskOperationResponse.getResponseMap().get("deletedRows")
        );
        assertThat(output.getOut().contains(responseLog));

    }

}
