package uk.gov.hmcts.reform.wataskmonitor.config.job;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "job.clean-up")
@Getter
@Setter
@ToString
public class CleanUpJobConfig {

    @Value("${environment}")
    private String environment;
    private String camundaMaxResults;
    private Long startedBeforeDays;
    private List<String> allowedEnvironment;

}
