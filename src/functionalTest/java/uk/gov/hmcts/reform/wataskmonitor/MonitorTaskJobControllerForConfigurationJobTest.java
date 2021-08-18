package uk.gov.hmcts.reform.wataskmonitor;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.wacaseeventhandler.TestUtility;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.MonitorTaskJobRequest;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LawOfDemeter"})
public class MonitorTaskJobControllerForConfigurationJobTest extends SpringBootFunctionalBaseTest {

    @Test
    public void givenMonitorTaskJobRequestShouldReturnStatus200AndExpectedResponse() {
        String body = TestUtility.asJsonString(new MonitorTaskJobRequest(new JobDetails(
            JobName.CONFIGURATION,
            "1000"
        )));
        given()
            .contentType(APPLICATION_JSON_VALUE)
            .header("ServiceAuthorization", serviceToken)
            .body(body)
            .when()
            .post("/monitor/tasks/jobs")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body(is(body));
    }

}
