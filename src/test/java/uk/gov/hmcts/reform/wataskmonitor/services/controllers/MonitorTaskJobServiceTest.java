package uk.gov.hmcts.reform.wataskmonitor.services.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation.InitiationJob;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.INITIATION;

@ExtendWith(MockitoExtension.class)
class MonitorTaskJobServiceTest {

    private static final String SERVICE_TOKEN = "serviceToken";

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Mock
    InitiationJob initiationJob;

    List<JobService> jobServices;

    MonitorTaskJobService monitorTaskJobService;

    @BeforeEach
    void setup() {

        jobServices = List.of(initiationJob);

        monitorTaskJobService = new MonitorTaskJobService(
            jobServices, authTokenGenerator
        );
    }

    @Test
    void should_run_initiation_job() {
        when(authTokenGenerator.generate())
            .thenReturn(SERVICE_TOKEN);

        when(initiationJob.canRun(INITIATION))
            .thenReturn(true);

        monitorTaskJobService.execute(INITIATION);

        verify(initiationJob, times(1)).run(SERVICE_TOKEN);
    }

    @Test
    void should_throw_exception_when_job_fails() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(initiationJob.canRun(INITIATION)).thenReturn(true);
        doThrow(new RuntimeException("Job failed")).when(initiationJob).run(SERVICE_TOKEN);

        CompletableFuture<String> future = monitorTaskJobService.execute(INITIATION);

        assertThrows(ExecutionException.class, future::get);
        verify(initiationJob, times(1)).run(SERVICE_TOKEN);
    }

}
