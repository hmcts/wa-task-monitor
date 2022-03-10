package uk.gov.hmcts.reform.wataskmonitor.config.job;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "job.termination")
@Getter
@Setter
@ToString
public class TerminationJobConfig {

    private String camundaMaxResults;
    private boolean camundaTimeLimitFlag;
    private Long camundaTimeLimit;

}
