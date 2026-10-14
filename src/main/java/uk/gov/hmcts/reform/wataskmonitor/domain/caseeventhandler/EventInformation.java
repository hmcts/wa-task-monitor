package uk.gov.hmcts.reform.wataskmonitor.domain.caseeventhandler;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.Locale;

@ToString
@Builder
@EqualsAndHashCode
@SuppressWarnings("PMD.ExcessiveParameterList")
public final class EventInformation {

    @NotEmpty
    private final String eventInstanceId;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private final LocalDateTime eventTimeStamp;
    @NotEmpty
    private final String caseId;
    @NotEmpty
    private final String jurisdictionId;
    @NotEmpty
    private final String caseTypeId;
    @NotEmpty
    private final String eventId;
    private final String previousStateId;
    private final String newStateId;
    @NotEmpty
    private final String userId;
    private final AdditionalData additionalData;

    @JsonCreator
    public EventInformation(@JsonProperty("EventInstanceId") @JsonAlias("event_instance_id")
                            String eventInstanceId,
                            @JsonProperty("EventTimeStamp") @JsonAlias("event_time_stamp")
                            LocalDateTime eventTimeStamp,
                            @JsonProperty("CaseId") @JsonAlias("case_id") String caseId,
                            @JsonProperty("JurisdictionId") @JsonAlias("jurisdiction_id")
                            String jurisdictionId,
                            @JsonProperty("CaseTypeId") @JsonAlias("case_type_id") String caseTypeId,
                            @JsonProperty("EventId") @JsonAlias("event_id") String eventId,
                            @JsonProperty("PreviousStateId") @JsonAlias("previous_state_id")
                            String previousStateId,
                            @JsonProperty("NewStateId") @JsonAlias("new_state_id") String newStateId,
                            @JsonProperty("UserId") @JsonAlias("user_id") String userId,
                            @JsonProperty("AdditionalData") @JsonAlias("additional_data")
                            AdditionalData additionalData) {
        this.eventInstanceId = eventInstanceId;
        this.eventTimeStamp = eventTimeStamp;
        this.caseId = caseId;
        this.jurisdictionId = jurisdictionId.toLowerCase(Locale.ENGLISH);
        this.caseTypeId = caseTypeId.toLowerCase(Locale.ENGLISH);
        this.eventId = eventId;
        this.previousStateId = previousStateId;
        this.newStateId = newStateId;
        this.userId = userId;
        this.additionalData = additionalData;
    }

    public String getEventInstanceId() {
        return eventInstanceId;
    }

    public LocalDateTime getEventTimeStamp() {
        return eventTimeStamp;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getJurisdictionId() {
        return jurisdictionId;
    }

    public String getCaseTypeId() {
        return caseTypeId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getPreviousStateId() {
        return previousStateId;
    }

    public String getNewStateId() {
        return newStateId;
    }

    public String getUserId() {
        return userId;
    }

    public AdditionalData getAdditionalData() {
        return additionalData;
    }
}
