package uk.gov.hmcts.reform.wataskmonitor.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public final class MonitorTaskJobReq {
    private final JobDetails jobDetails;

    public MonitorTaskJobReq(@JsonProperty("job_details") JobDetails jobDetails) {
        this.jobDetails = jobDetails;
    }

    public JobDetails getJobDetails() {
        return jobDetails;
    }

}
