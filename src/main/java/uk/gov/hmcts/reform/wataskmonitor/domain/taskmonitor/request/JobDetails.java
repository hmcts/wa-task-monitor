package uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

@ToString
@EqualsAndHashCode
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class JobDetails {
    private final JobName name;
    private final String camundaMaxResults;

    @JsonCreator
    public JobDetails(@JsonProperty("name") JobName name,
                      @JsonProperty("camundaMaxResults") String camundaMaxResults) {
        this.name = name;
        this.camundaMaxResults = camundaMaxResults;
    }

}
