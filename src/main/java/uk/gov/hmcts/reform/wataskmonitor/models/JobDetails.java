package uk.gov.hmcts.reform.wataskmonitor.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public final class JobDetails {
    private final String name;

    public JobDetails(@JsonProperty("name") String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
