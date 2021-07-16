package uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class MonitorTaskJobRequest {
    private final JobDetails jobDetails;

    @JsonCreator
    public MonitorTaskJobRequest(JobDetails jobDetails) {
        this.jobDetails = jobDetails;
    }

    public JobDetails getJobDetails() {
        return jobDetails;
    }

}
