package uk.gov.hmcts.reform.wataskmonitor.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wacaseeventhandler.TestUtility;
import uk.gov.hmcts.reform.wacaseeventhandler.matchers.CamundaQueryParametersMatcher;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskConfigurationClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.MonitorTaskJobRequest;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.wacaseeventhandler.controllers.MonitorTaskJobControllerUtility.expectedResponse;

class MonitorTaskJobControllerForConfigurationJobTest extends SpringBootIntegrationBaseTest {

    public static final String SERVICE_TOKEN = "some service token";
    public static final String CAMUNDA_TASK_ID = "some camunda task id";

    @MockBean
    private CamundaClient camundaClient;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private TaskConfigurationClient taskConfigurationClient;

    @BeforeEach
    void setUp() {
        mockExternalDependencies();
    }

    private void mockExternalDependencies() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);

        when(camundaClient.getTasks(
            eq(SERVICE_TOKEN),
            eq("0"),
            eq("1000"),
            argThat(new CamundaQueryParametersMatcher(TestUtility.getExpectedRequestForUnconfiguredTasks()))
        )).thenReturn(List.of(new CamundaTask(
            CAMUNDA_TASK_ID,
            "task name",
            "2151a580-c3c3-11eb-8b76-d26a7287fec2"
        )));

        when(taskConfigurationClient.configureTask(eq(SERVICE_TOKEN), eq(CAMUNDA_TASK_ID)))
            .thenReturn("OK");
    }

    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LawOfDemeter"})
    @Test
    public void givenMonitorTaskJobRequestShouldReturnStatus200AndExpectedResponse() throws Exception {
        MonitorTaskJobRequest monitorTaskJobReq = new MonitorTaskJobRequest(new JobDetails(
            JobName.CONFIGURATION,
            "1000"
        ));

        mockMvc.perform(post("/monitor/tasks/jobs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(TestUtility.asJsonString(monitorTaskJobReq)))
            .andExpect(status().isOk())
            .andExpect(content().string(equalTo(expectedResponse.apply(JobName.CONFIGURATION.name()))));

        verify(authTokenGenerator).generate();
        verify(camundaClient).getTasks(
            eq(SERVICE_TOKEN),
            eq("0"),
            eq("1000"),
            argThat(new CamundaQueryParametersMatcher(TestUtility.getExpectedRequestForUnconfiguredTasks()))
        );
        verify(taskConfigurationClient).configureTask(eq(SERVICE_TOKEN), eq(CAMUNDA_TASK_ID));
    }

}
