package uk.gov.hmcts.reform.wataskmonitor.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.TestUtility;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.MonitorTaskJobRequest;
import uk.gov.hmcts.reform.wataskmonitor.services.ResourceEnum;
import uk.gov.hmcts.reform.wataskmonitor.utils.ResourceUtility;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.wataskmonitor.controllers.MonitorTaskJobControllerUtility.expectedResponse;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.AD_HOC_DELETE_PROCESS_INSTANCES;

class MonitorTaskJobControllerForAdHocJobTest extends SpringBootIntegrationBaseTest {

    public static final String SERVICE_TOKEN = "some service token";

    @MockBean
    private CamundaClient camundaClient;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    private String requestParameter;

    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LawOfDemeter"})
    @Test
    public void givenMonitorTaskJobRequestShouldReturnStatus200AndExpectedResponse() throws Exception {
        MonitorTaskJobRequest monitorTaskJobReq = new MonitorTaskJobRequest(new JobDetails(
            AD_HOC_DELETE_PROCESS_INSTANCES));

        mockMvc.perform(
                post("/monitor/tasks/jobs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf8")
                    .content(TestUtility.asJsonString(monitorTaskJobReq)))
            .andExpect(status().isOk())
            .andExpect(content().string(equalTo(expectedResponse.apply(AD_HOC_DELETE_PROCESS_INSTANCES.name()))));

        verify(authTokenGenerator).generate();
        verify(camundaClient).deleteProcessInstance(eq(SERVICE_TOKEN), eq(requestParameter));
    }

    @BeforeEach
    void setUp() {
        mockExternalDependencies();
    }

    private void mockExternalDependencies() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);

        requestParameter = ResourceUtility.getResource(ResourceEnum.DELETE_PROCESS_INSTANCES_JOB_SERVICE);
        String someResponse = "{\"id\": \"78e1a849-d9b3-11eb-bb4f-d62f1f620fc5\",\"type\": \"instance-deletion\" }";
        when(camundaClient.deleteProcessInstance(eq(SERVICE_TOKEN), eq(requestParameter)))
            .thenReturn(someResponse);
    }

}
