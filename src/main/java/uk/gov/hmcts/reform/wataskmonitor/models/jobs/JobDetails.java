package uk.gov.hmcts.reform.wataskmonitor.models.jobs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public final class JobDetails {
    private final JobDetailName name;

    public JobDetails(@JsonProperty("name") JobDetailName name) {
        this.name = name;
    }

    public JobDetailName getName() {
        return name;
    }
}
