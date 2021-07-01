package uk.gov.hmcts.reform.wataskmonitor;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wacaseeventhandler.TestUtility;
import uk.gov.hmcts.reform.wataskmonitor.models.MonitorTaskJobReq;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetailName;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetails;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wacaseeventhandler.controllers.MonitorTaskJobControllerUtility.expectedResponse;

@SpringBootTest
@ActiveProfiles({"local", "functional"})
@RunWith(SpringIntegrationSerenityRunner.class)
@Slf4j
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LawOfDemeter"})
class MonitorTaskJobControllerForAdHocJobTest {

    @Value("${targets.instance}")
    protected String testUrl;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;
    private String serviceToken;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = testUrl;
        RestAssured.useRelaxedHTTPSValidation();

        serviceToken = authTokenGenerator.generate();
    }

    @Test
    void givenMonitorTaskJobRequestShouldReturnStatus200AndExpectedResponse() {
        given()
            .contentType(APPLICATION_JSON_VALUE)
            .header("ServiceAuthorization", serviceToken)
            .body(TestUtility.asJsonString(new MonitorTaskJobReq(new JobDetails(JobDetailName.AD_HOC))))
            .when()
            .post("/monitor/tasks/jobs")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body(is(expectedResponse.apply(JobDetailName.AD_HOC.name())));
    }

    @Test
    void givenMonitorTaskJobRequestWithNoServiceAuthenticationHeaderShouldReturnStatus401Response() {
        given()
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .post("/monitor/tasks/jobs")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

}
