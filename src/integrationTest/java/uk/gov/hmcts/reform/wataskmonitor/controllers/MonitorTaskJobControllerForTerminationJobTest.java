package uk.gov.hmcts.reform.wataskmonitor.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.TestUtility;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskManagementClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.TerminateTaskRequest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.options.TerminateInfo;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.MonitorTaskJobRequest;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.wataskmonitor.controllers.MonitorTaskJobControllerUtility.expectedResponse;

class MonitorTaskJobControllerForTerminationJobTest extends SpringBootIntegrationBaseTest {

    public static final String SERVICE_TOKEN = "some service token";
    public static final String CAMUNDA_TASK_ID_FOR_CANCELLATION = "some camunda task id for cancellation";
    public static final String CAMUNDA_TASK_ID_FOR_COMPLETION = "some camunda task id for completion";
    public static final String CAMUNDA_TASK_ID_FOR_DELETED = "some camunda task id for deletion";

    @MockitoBean
    private CamundaClient camundaClient;
    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;
    @MockitoBean
    private TaskManagementClient taskManagementClient;

    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LawOfDemeter"})
    @Test
    public void shouldSucceedAndTerminateTasks() throws Exception {
        MonitorTaskJobRequest monitorTaskJobReq = new MonitorTaskJobRequest(new JobDetails(JobName.TERMINATION));

        mockMvc.perform(post("/monitor/tasks/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtility.asJsonString(monitorTaskJobReq)))
            .andExpect(status().isOk())
            .andExpect(content().string(equalTo(expectedResponse.apply(JobName.TERMINATION.name()))));

        verify(authTokenGenerator).generate();
        verify(camundaClient).getTasksFromHistory(
            eq(SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            any()
        );

        verifyTerminateEndpointWasCalledWithReason("cancelled", 1, CAMUNDA_TASK_ID_FOR_CANCELLATION);
        verifyTerminateEndpointWasCalledWithReason("completed", 1, CAMUNDA_TASK_ID_FOR_COMPLETION);
        verifyTerminateEndpointWasCalledWithReason("deleted", 1, CAMUNDA_TASK_ID_FOR_DELETED);

    }

    @BeforeEach
    void setUp() {
        mockExternalDependencies();
    }

    private void verifyTerminateEndpointWasCalledWithReason(String terminateReason,
                                                            int times,
                                                            String taskId) {
        TerminateTaskRequest request = new TerminateTaskRequest(new TerminateInfo(terminateReason));
        verify(taskManagementClient, times(times)).terminateTask(SERVICE_TOKEN, taskId, request);
    }

    private void mockExternalDependencies() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);

        when(camundaClient.getTasksFromHistory(
            eq(SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            any()
        )).thenReturn(
            List.of(
                new HistoricCamundaTask(CAMUNDA_TASK_ID_FOR_CANCELLATION, "cancelled",
                    null, null),
                new HistoricCamundaTask(CAMUNDA_TASK_ID_FOR_COMPLETION, "completed",
                    null, null),
                new HistoricCamundaTask(CAMUNDA_TASK_ID_FOR_DELETED, "deleted",
                    null, null)
            )
        );

        doNothing().when(taskManagementClient).terminateTask(any(), any(), any());
    }

}
