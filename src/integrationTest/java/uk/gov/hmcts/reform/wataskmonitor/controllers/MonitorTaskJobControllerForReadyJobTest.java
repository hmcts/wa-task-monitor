package uk.gov.hmcts.reform.wataskmonitor.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.TestUtility;
import uk.gov.hmcts.reform.wataskmonitor.clients.CaseEventHandlerClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.MonitorTaskJobRequest;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.wataskmonitor.controllers.MonitorTaskJobControllerUtility.expectedResponse;

class MonitorTaskJobControllerForReadyJobTest extends SpringBootIntegrationBaseTest {

    public static final String SERVICE_TOKEN = "some service token";

    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private CaseEventHandlerClient caseEventHandlerClient;

    @BeforeEach
    void setUp() {
        mockExternalDependencies();
    }

    @Test
    public void shouldSucceedAndInitiateTasks() throws Exception {
        MonitorTaskJobRequest monitorTaskJobReq = new MonitorTaskJobRequest(new JobDetails(JobName.READY));

        mockMvc.perform(post("/monitor/tasks/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtility.asJsonString(monitorTaskJobReq)))
            .andExpect(status().isOk())
            .andExpect(content().string(equalTo(expectedResponse.apply(JobName.READY.name()))));

        verify(authTokenGenerator).generate();
        verify(caseEventHandlerClient).findProblematicMessages(
            eq(SERVICE_TOKEN),
            eq(JobName.READY.name())
        );

        verify(caseEventHandlerClient, times(1))
            .findProblematicMessages(eq(SERVICE_TOKEN), eq(JobName.READY.name()));
    }


    private void mockExternalDependencies() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);

        String messageId = "Some message Id";

        when(caseEventHandlerClient.findProblematicMessages(
            eq(SERVICE_TOKEN),
            eq(JobName.READY.name())
        )).thenReturn(
            List.of(messageId)
        );
    }
}
