package uk.gov.hmcts.reform.wataskmonitor;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wacaseeventhandler.TestUtility;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.MonitorTaskJobRequest;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wacaseeventhandler.controllers.MonitorTaskJobControllerUtility.expectedResponse;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.AD_HOC_DELETE_PROCESS_INSTANCES;

@SpringBootTest
@ActiveProfiles({"functional"})
@RunWith(SpringIntegrationSerenityRunner.class)
@Slf4j
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LawOfDemeter"})
@Disabled
class MonitorTaskJobControllerForAdHocDeleteProcessInstanceTest {

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
            .body(TestUtility.asJsonString(new MonitorTaskJobRequest(new JobDetails(AD_HOC_DELETE_PROCESS_INSTANCES))))
            .when()
            .post("/monitor/tasks/jobs")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body(is(expectedResponse.apply(AD_HOC_DELETE_PROCESS_INSTANCES.name())));
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
