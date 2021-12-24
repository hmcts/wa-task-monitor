package uk.gov.hmcts.reform.wataskmonitor.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.MonitorTaskJobRequest;
import uk.gov.hmcts.reform.wataskmonitor.services.controllers.MonitorTaskJobService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MonitorTaskJobControllerTest {

    @Mock
    private MonitorTaskJobService monitorTaskJobService;

    private MonitorTaskJobController monitorTaskJobController;

    @BeforeEach
    public void setup() {
        monitorTaskJobController = new MonitorTaskJobController(monitorTaskJobService);
    }

    @Test
    public void should_succeed_and_return_job_name() {
        MonitorTaskJobRequest monitorTaskJobRequest = new MonitorTaskJobRequest(new JobDetails(JobName.CONFIGURATION));

        MonitorTaskJobRequest response = monitorTaskJobController.monitorTaskJob(monitorTaskJobRequest);
        assertEquals(JobName.CONFIGURATION, response.getJobDetails().getName());
        verify(monitorTaskJobService, times(1)).execute(monitorTaskJobRequest.getJobDetails().getName());
    }

}