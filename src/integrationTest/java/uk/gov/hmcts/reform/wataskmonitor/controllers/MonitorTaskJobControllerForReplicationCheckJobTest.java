package uk.gov.hmcts.reform.wataskmonitor.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.TestUtility;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskOperationClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.TaskOperationRequest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.response.TaskOperationResponse;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.MonitorTaskJobRequest;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.wataskmonitor.controllers.MonitorTaskJobControllerUtility.expectedResponse;

class MonitorTaskJobControllerForReplicationCheckJobTest extends SpringBootIntegrationBaseTest {

    public static final String SERVICE_TOKEN = "some service token";

    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;
    @MockitoBean
    private TaskOperationClient taskOperationClient;
    @MockitoBean
    private TaskOperationRequest taskOperationRequest;

    @BeforeEach
    void setUp() {
        mockExternalDependencies();
    }

    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LawOfDemeter"})
    @Test
    public void givenMonitorTaskJobRequestShouldReturnStatus200() throws Exception {
        MonitorTaskJobRequest monitorTaskJobReq = new MonitorTaskJobRequest(
                new JobDetails(JobName.PERFORM_REPLICATION_CHECK));

        mockMvc.perform(post("/monitor/tasks/jobs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(TestUtility.asJsonString(monitorTaskJobReq)))
            .andExpect(status().isOk())
            .andExpect(content().string(equalTo(expectedResponse.apply(JobName.PERFORM_REPLICATION_CHECK.name()))));

        verify(authTokenGenerator).generate();
        verify(taskOperationClient).executeOperation(eq(SERVICE_TOKEN), any(TaskOperationRequest.class));
    }

    private void mockExternalDependencies() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        TaskOperationResponse taskOperationResponse = new TaskOperationResponse(Map.of());

        when(taskOperationClient.executeOperation(eq(SERVICE_TOKEN), eq(taskOperationRequest)))
            .thenReturn(taskOperationResponse);
    }

}
