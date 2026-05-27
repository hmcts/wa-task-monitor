package uk.gov.hmcts.reform.wataskmonitor.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.TestUtility;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.MonitorTaskJobRequest;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.maintenance.DatabaseMaintenanceExecutorService;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MonitorTaskJobControllerFoDatabaseMaintenanceJobTest extends SpringBootIntegrationBaseTest {

    public static final String SERVICE_TOKEN = "some service token";

    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;
    @MockitoBean
    private DatabaseMaintenanceExecutorService databaseMaintenanceExecutorService;

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
    }

    @Test
    void givenMonitorTaskJobRequestShouldReturnStatus200() throws Exception {
        MonitorTaskJobRequest request = new MonitorTaskJobRequest(new JobDetails(JobName.DATABASE_MAINTENANCE));

        mockMvc.perform(post("/monitor/tasks/jobs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(TestUtility.asJsonString(request)))
            .andExpect(status().isOk())
            .andExpect(content().string(equalTo(TestUtility.asJsonString(request))));

        verify(authTokenGenerator).generate();
        verify(databaseMaintenanceExecutorService).executeConfiguredMaintenance();
    }
}
