package uk.gov.hmcts.reform.wataskmonitor.config.job;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "job.initiation")
@Getter
@Setter
public class InitiationJobConfig {
    String camundaMaxResults;
}
