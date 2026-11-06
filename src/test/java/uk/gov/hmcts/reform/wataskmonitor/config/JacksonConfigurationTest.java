package uk.gov.hmcts.reform.wataskmonitor.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.reform.wataskmonitor.domain.caseeventhandler.EventInformation;
import uk.gov.hmcts.reform.wataskmonitor.domain.idam.Token;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@Import(JacksonConfiguration.class)
class JacksonConfigurationTest {

    @Autowired
    private JacksonTester<Token> tokenTester;

    @Autowired
    private JacksonTester<EventInformation> eventInformationTester;

    @Test
    void tokenRoundTripUsesSnakeCase() throws Exception {
        String json = tokenTester.write(new Token("abc", "openid")).getJson();

        assertThat(json).contains("\"access_token\"");
        assertThat(json).contains("\"abc\"");
        assertThat(json).contains("\"scope\"");
        assertThat(json).contains("\"openid\"");
        assertThat(json).doesNotContain("accessToken");

        assertThat(tokenTester.parseObject(json).getAccessToken()).isEqualTo("abc");
        assertThat(tokenTester.parseObject(json).getScope()).isEqualTo("openid");
    }

    @Test
    void eventInformationRoundTripKeepsExplicitPropertyNames() throws Exception {
        LocalDateTime timestamp = LocalDateTime.of(2024, 3, 15, 10, 11, 12);
        EventInformation event = new EventInformation(
            "event-1",
            timestamp,
            "123",
            "IA",
            "Asylum",
            "submit",
            null,
            "stateB",
            "user-1",
            null
        );

        String json = eventInformationTester.write(event).getJson();

        assertThat(json).contains("\"event_instance_id\"");
        assertThat(json).contains("\"event-1\"");
        assertThat(json).contains("\"event_time_stamp\"");
        assertThat(json).contains("\"case_type_id\"");
        assertThat(json).contains("\"asylum\"");
        assertThat(json).doesNotContain("EventInstanceId");
        assertThat(json).doesNotContain("eventInstanceId");
        assertThat(json).doesNotContain("previous_state_id");

        EventInformation read = eventInformationTester.parseObject(json);
        assertThat(read.getEventInstanceId()).isEqualTo("event-1");
        assertThat(read.getEventTimeStamp()).isEqualTo(timestamp);
        assertThat(read.getJurisdictionId()).isEqualTo("ia");
        assertThat(read.getCaseTypeId()).isEqualTo("asylum");
        assertThat(read.getPreviousStateId()).isNull();

        EventInformation fromPascalCase = eventInformationTester.parseObject("""
            {
              "EventInstanceId": "event-1",
              "EventTimeStamp": "2024-03-15T10:11:12",
              "CaseId": "123",
              "JurisdictionId": "IA",
              "CaseTypeId": "Asylum",
              "EventId": "submit",
              "NewStateId": "stateB",
              "UserId": "user-1"
            }
            """);
        assertThat(fromPascalCase.getEventInstanceId()).isEqualTo("event-1");
        assertThat(fromPascalCase.getEventTimeStamp()).isEqualTo(timestamp);
        assertThat(fromPascalCase.getJurisdictionId()).isEqualTo("ia");
        assertThat(fromPascalCase.getCaseTypeId()).isEqualTo("asylum");
    }

}
