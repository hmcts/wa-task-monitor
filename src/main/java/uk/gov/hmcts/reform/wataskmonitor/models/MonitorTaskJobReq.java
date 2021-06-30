package uk.gov.hmcts.reform.wataskmonitor.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetails;

@ToString
@EqualsAndHashCode
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public final class MonitorTaskJobReq {
    private final JobDetails jobDetail;

    public MonitorTaskJobReq(@JsonProperty("job_details") JobDetails jobDetail) {
        this.jobDetail = jobDetail;
    }

    public JobDetails getJobDetail() {
        return jobDetail;
    }

}
