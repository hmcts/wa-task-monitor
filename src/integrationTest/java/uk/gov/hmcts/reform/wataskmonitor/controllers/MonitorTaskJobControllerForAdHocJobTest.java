package uk.gov.hmcts.reform.wataskmonitor.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wacaseeventhandler.TestUtility;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.controllers.request.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.controllers.request.MonitorTaskJobRequest;
import uk.gov.hmcts.reform.wataskmonitor.utils.ResourceUtility;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.wacaseeventhandler.controllers.MonitorTaskJobControllerUtility.expectedResponse;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.AD_HOC_DELETE_PROCESS_INSTANCES;
import static uk.gov.hmcts.reform.wataskmonitor.services.jobs.RequestsEnum.DELETE_PROCESS_INSTANCES_JOB_SERVICE;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles({"integration"})
class MonitorTaskJobControllerForAdHocJobTest {

    public static final String SERVICE_TOKEN = "some service token";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CamundaClient camundaClient;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    private String requestParameter;

    @BeforeEach
    void setUp() {
        mockExternalDependencies();
    }

    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LawOfDemeter"})
    @Test
    public void givenMonitorTaskJobRequestShouldReturnStatus200AndExpectedResponse() throws Exception {
        MonitorTaskJobRequest monitorTaskJobReq = new MonitorTaskJobRequest(new JobDetails(
            AD_HOC_DELETE_PROCESS_INSTANCES));


        mockMvc.perform(post("/monitor/tasks/jobs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(TestUtility.asJsonString(monitorTaskJobReq)))
            .andExpect(status().isOk())
            .andExpect(content().string(equalTo(expectedResponse.apply(AD_HOC_DELETE_PROCESS_INSTANCES.name()))));

        verify(authTokenGenerator).generate();
        verify(camundaClient).deleteProcessInstance(eq(SERVICE_TOKEN), eq(requestParameter));
    }

    private void mockExternalDependencies() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);

        requestParameter = ResourceUtility.getResource(DELETE_PROCESS_INSTANCES_JOB_SERVICE.getRequestParameterBody());
        String someResponse = "{\"id\": \"78e1a849-d9b3-11eb-bb4f-d62f1f620fc5\",\"type\": \"instance-deletion\" }";
        when(camundaClient.deleteProcessInstance(eq(SERVICE_TOKEN), eq(requestParameter)))
            .thenReturn(someResponse);
    }

}
