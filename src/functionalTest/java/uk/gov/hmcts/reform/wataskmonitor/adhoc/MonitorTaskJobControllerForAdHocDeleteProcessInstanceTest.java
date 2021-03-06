package uk.gov.hmcts.reform.wataskmonitor.adhoc;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.wataskmonitor.SpringBootFunctionalBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.TestUtility;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.MonitorTaskJobRequest;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wataskmonitor.config.SecurityConfiguration.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.wataskmonitor.controllers.MonitorTaskJobControllerUtility.expectedResponse;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.AD_HOC_DELETE_PROCESS_INSTANCES;

@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LawOfDemeter"})
public class MonitorTaskJobControllerForAdHocDeleteProcessInstanceTest extends SpringBootFunctionalBaseTest {

    @Test
    public void givenMonitorTaskJobRequestShouldReturnStatus200AndExpectedResponse() {
        given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORIZATION, serviceToken)
            .body(TestUtility.asJsonString(new MonitorTaskJobRequest(new JobDetails(AD_HOC_DELETE_PROCESS_INSTANCES))))
            .when()
            .post("/monitor/tasks/jobs")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body(is(expectedResponse.apply(AD_HOC_DELETE_PROCESS_INSTANCES.name())));
    }

}
