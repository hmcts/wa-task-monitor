package uk.gov.hmcts.reform.wataskmonitor.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wacaseeventhandler.TestUtility;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskManagementClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.TerminateTaskRequest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TerminateReason;
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
import static uk.gov.hmcts.reform.wacaseeventhandler.controllers.MonitorTaskJobControllerUtility.expectedResponse;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TerminateReason.CANCELLED;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.TerminateReason.COMPLETED;

class MonitorTaskJobControllerForTerminationJobTest extends SpringBootIntegrationBaseTest {

    public static final String SERVICE_TOKEN = "some service token";
    public static final String CAMUNDA_TASK_ID_FOR_CANCELLATION = "some camunda task id for cancellation";
    public static final String CAMUNDA_TASK_ID_FOR_COMPLETION = "some camunda task id for completion";

    @MockBean
    private CamundaClient camundaClient;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private TaskManagementClient taskManagementClient;

    @BeforeEach
    void setUp() {
        mockExternalDependencies();
    }

    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LawOfDemeter"})
    @Test
    public void shouldSucceedAndTerminateTasks() throws Exception {
        MonitorTaskJobRequest monitorTaskJobReq = new MonitorTaskJobRequest(new JobDetails(
            JobName.TERMINATION,
            "1000"
        ));

        mockMvc.perform(post("/monitor/tasks/jobs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(TestUtility.asJsonString(monitorTaskJobReq)))
            .andExpect(status().isOk())
            .andExpect(content().string(equalTo(expectedResponse.apply(JobName.TERMINATION.name()))));

        verify(authTokenGenerator).generate();
        verify(camundaClient).getTasksFromHistory(
            eq(SERVICE_TOKEN),
            eq("0"),
            eq("1000"),
            any()
        );

        verifyTerminateEndpointWasCalledWithTerminateReason(CANCELLED, 1, CAMUNDA_TASK_ID_FOR_CANCELLATION);
        verifyTerminateEndpointWasCalledWithTerminateReason(COMPLETED, 1, CAMUNDA_TASK_ID_FOR_COMPLETION);
    }

    private void verifyTerminateEndpointWasCalledWithTerminateReason(TerminateReason terminateReason,
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
            eq("1000"),
            any()
        )).thenReturn(
            List.of(
                new HistoricCamundaTask(CAMUNDA_TASK_ID_FOR_CANCELLATION, "cancelled"),
                new HistoricCamundaTask(CAMUNDA_TASK_ID_FOR_COMPLETION, "completed")
            )
        );

        doNothing().when(taskManagementClient).terminateTask(any(), any(), any());
    }

}
