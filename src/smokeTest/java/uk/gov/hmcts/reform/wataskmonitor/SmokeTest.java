package uk.gov.hmcts.reform.wataskmonitor;

import io.restassured.RestAssured;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

@SuppressWarnings({"PMD.LawOfDemeter", "PMD.JUnitTestsShouldIncludeAssert"})
class SmokeTest {

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:8077"
        );

    @Test
    void shouldCheckServiceAndReturnWelcomeMessage() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();

        when()
            .get("/")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(containsString("Welcome to wa-task-monitor"));
    }
}
