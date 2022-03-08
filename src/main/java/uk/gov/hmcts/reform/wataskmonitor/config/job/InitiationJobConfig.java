package uk.gov.hmcts.reform.wataskmonitor.config.job;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.wataskmonitor.config.entity.Migration;

@Configuration
@ConfigurationProperties(prefix = "job.initiation")
@Getter
@Setter
@ToString
public class InitiationJobConfig {

    private String camundaMaxResults;
    private boolean camundaTimeLimitFlag;
    private Long camundaTimeLimit;

    private Migration migration;
}
