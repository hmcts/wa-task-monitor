package uk.gov.hmcts.reform.wataskmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.ccd.client",
    "uk.gov.hmcts.reform.wataskmonitor.clients",
    "uk.gov.hmcts.reform.wataskmonitor.services",
    "uk.gov.hmcts.reform.authorisation"
})
@EnableAsync
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
