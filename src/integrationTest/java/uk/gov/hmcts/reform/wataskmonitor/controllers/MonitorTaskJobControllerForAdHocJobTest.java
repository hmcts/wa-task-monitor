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
import uk.gov.hmcts.reform.wataskmonitor.models.MonitorTaskJobReq;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetailName;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetails;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.wacaseeventhandler.controllers.MonitorTaskJobControllerUtility.expectedResponse;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles({"local", "integration"})
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

    private void mockExternalDependencies() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);

        requestParameter = "{\n"
                           + "  \"deleteReason\": \"clean up running process instances\",\n"
                           + "  \"processInstanceIds\": [\n"
                           + "    \"4e9f1401-d993-11eb-8fe1-82fee8519111\",\n"
                           + "    \"48d26599-d993-11eb-9a97-5a7b203c8112\"\n"
                           + "  ],\n"
                           + "  \"skipCustomListeners\": true,\n"
                           + "  \"skipSubprocesses\": true,\n"
                           + "  \"failIfNotExists\": false\n"
                           + "}\n";
        String someResponse = "{\"id\": \"78e1a849-d9b3-11eb-bb4f-d62f1f620fc5\",\"type\": \"instance-deletion\" }";
        when(camundaClient.deleteProcessInstance(eq(SERVICE_TOKEN), eq(requestParameter)))
            .thenReturn(someResponse);
    }

    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LawOfDemeter"})
    @Test
    public void givenMonitorTaskJobRequestShouldReturnStatus200AndExpectedResponse() throws Exception {
        MonitorTaskJobReq monitorTaskJobReq = new MonitorTaskJobReq(new JobDetails(JobDetailName.AD_HOC));


        mockMvc.perform(post("/monitor/tasks/jobs")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtility.asJsonString(monitorTaskJobReq)))
            .andExpect(status().isOk())
            .andExpect(content().string(equalTo(expectedResponse.apply(JobDetailName.AD_HOC.name()))));

        verify(authTokenGenerator).generate();
        verify(camundaClient).deleteProcessInstance(eq(SERVICE_TOKEN), eq(requestParameter));
    }

}
