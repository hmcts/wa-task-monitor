package uk.gov.hmcts.reform.wataskmonitor.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.TestUtility;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskOperationClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.TaskOperationRequest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.response.TaskOperationResponse;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.MonitorTaskJobRequest;
import uk.gov.hmcts.reform.wataskmonitor.services.controllers.MonitorTaskJobService;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.wataskmonitor.controllers.MonitorTaskJobControllerUtility.expectedResponse;

class MonitorTaskJobControllerFoReconfigurationJobTest extends SpringBootIntegrationBaseTest {

    public static final String SERVICE_TOKEN = "some service token";

    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private TaskOperationClient taskOperationClient;
    @MockBean
    private TaskOperationRequest taskOperationRequest;

    @MockBean
    private MonitorTaskJobService monitorTaskJobService;

    @BeforeEach
    void setUp() {
        mockExternalDependencies();
    }

    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LawOfDemeter"})
    @Test
    public void given_monitor_task_job_request_should_return_status_200() throws Exception {
        MonitorTaskJobRequest monitorTaskJobReq = new MonitorTaskJobRequest(new JobDetails(JobName.RECONFIGURATION));

        mockMvc.perform(post("/monitor/tasks/jobs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(TestUtility.asJsonString(monitorTaskJobReq)))
            .andExpect(status().isOk())
            .andExpect(content().string(equalTo(expectedResponse.apply(JobName.RECONFIGURATION.name()))));

        verify(authTokenGenerator).generate();
        verify(taskOperationClient).executeOperation(eq(SERVICE_TOKEN), any(TaskOperationRequest.class));
    }

    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LawOfDemeter"})
    @Test
    public void given_monitor_task_job_request_should_throw_exception() throws Exception {
        MonitorTaskJobRequest monitorTaskJobReq = new MonitorTaskJobRequest(new JobDetails(JobName.RECONFIGURATION));

        CompletableFuture<String> future = CompletableFuture.failedFuture(new RuntimeException("Job execution failed"));
        when(monitorTaskJobService.execute(monitorTaskJobReq.getJobDetails().getName())).thenReturn(future);

        mockMvc.perform(post("/monitor/tasks/jobs")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtility.asJsonString(monitorTaskJobReq)))
            .andExpect(status().isInternalServerError());
    }

    private void mockExternalDependencies() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        TaskOperationResponse taskOperationResponse = new TaskOperationResponse(Map.of());

        when(taskOperationClient.executeOperation(eq(SERVICE_TOKEN), eq(taskOperationRequest)))
            .thenReturn(taskOperationResponse);
    }

}
