package uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class MonitorTaskJobRequest {
    private final JobDetails jobDetails;

    @JsonCreator
    public MonitorTaskJobRequest(@JsonProperty("job_details") JobDetails jobDetails) {
        this.jobDetails = jobDetails;
    }

    public JobDetails getJobDetails() {
        return jobDetails;
    }

}
