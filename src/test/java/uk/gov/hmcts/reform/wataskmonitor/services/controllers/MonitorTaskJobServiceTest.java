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
    public void setup() {

        jobServices = List.of(initiationJob);

        monitorTaskJobService = new MonitorTaskJobService(
            jobServices, authTokenGenerator
        );
    }

    @Test
    public void should_run_initiation_job() {
        when(authTokenGenerator.generate())
            .thenReturn(SERVICE_TOKEN);

        when(initiationJob.canRun(INITIATION))
            .thenReturn(true);

        monitorTaskJobService.execute(INITIATION);

        verify(initiationJob, times(1)).run(SERVICE_TOKEN);
    }

}
