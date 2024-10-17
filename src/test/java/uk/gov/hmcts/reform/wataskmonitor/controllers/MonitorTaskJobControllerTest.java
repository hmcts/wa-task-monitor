package uk.gov.hmcts.reform.wataskmonitor.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.MonitorTaskJobRequest;
import uk.gov.hmcts.reform.wataskmonitor.services.controllers.MonitorTaskJobService;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonitorTaskJobControllerTest {

    @Mock
    private MonitorTaskJobService monitorTaskJobService;

    private MonitorTaskJobController monitorTaskJobController;

    @BeforeEach
    void setup() {
        monitorTaskJobService = Mockito.mock(MonitorTaskJobService.class);
        monitorTaskJobController = new MonitorTaskJobController(monitorTaskJobService);
    }

    @Test
    void should_succeed_and_return_job_name() {
        MonitorTaskJobRequest monitorTaskJobRequest = new MonitorTaskJobRequest(new JobDetails(JobName.INITIATION));
        CompletableFuture<String> future = CompletableFuture.completedFuture("Success");
        when(monitorTaskJobService.execute(monitorTaskJobRequest.getJobDetails().getName())).thenReturn(future);

        ResponseEntity<MonitorTaskJobRequest> response = monitorTaskJobController.monitorTaskJob(monitorTaskJobRequest);
        assertEquals(JobName.INITIATION, response.getBody().getJobDetails().getName());
        verify(monitorTaskJobService, times(1)).execute(monitorTaskJobRequest.getJobDetails().getName());
    }

    @Test
    void should_return_internal_server_error_when_exception_thrown() {
        MonitorTaskJobRequest monitorTaskJobRequest = new MonitorTaskJobRequest(new JobDetails(JobName.INITIATION));

        CompletableFuture<String> future = CompletableFuture.failedFuture(new RuntimeException("Job execution failed"));
        when(monitorTaskJobService.execute(monitorTaskJobRequest.getJobDetails().getName())).thenReturn(future);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            monitorTaskJobController.monitorTaskJob(monitorTaskJobRequest);
        });
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        verify(monitorTaskJobService, times(1)).execute(monitorTaskJobRequest.getJobDetails().getName());
    }

}
