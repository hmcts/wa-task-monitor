package uk.gov.hmcts.reform.wataskmonitor.domain.camunda;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class CamundaTaskCount {
    private Long count;

    private CamundaTaskCount() {
    }

    public CamundaTaskCount(Long count) {
        this.count = count;
    }

    public Long getCount() {
        return count;
    }

}
