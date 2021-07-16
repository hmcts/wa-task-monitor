package uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

@ToString
@EqualsAndHashCode
public class JobDetails {
    private final JobName name;

    @JsonCreator
    public JobDetails(JobName name) {
        this.name = name;
    }

    public JobName getName() {
        return name;
    }
}
