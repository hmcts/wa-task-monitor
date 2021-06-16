package uk.gov.hmcts.reform.wataskmonitor.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.wataskmonitor.models.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.models.MonitorTaskJobReq;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class MonitorTaskJobTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    @Test
    public void givenMonitorTaskJobRequestShouldReturnStatus200AndExpectedResponse() throws Exception {
        MonitorTaskJobReq monitorTaskJobReq = new MonitorTaskJobReq(new JobDetails("some name"));
        String expectedResponse = "{\"job_details\":{\"name\":\"some name\"}}";

        mockMvc.perform(post("/monitor/tasks/jobs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(monitorTaskJobReq)))
            .andExpect(status().isOk())
            .andExpect(content().string(equalTo(expectedResponse)));
    }

    private String asJsonString(MonitorTaskJobReq monitorTaskJobReq) {
        try {
            return new ObjectMapper().writeValueAsString(monitorTaskJobReq);
        } catch (JsonProcessingException e) {
            fail("can't serialise object: " + monitorTaskJobReq, e);
            return StringUtils.EMPTY;
        }
    }

}
