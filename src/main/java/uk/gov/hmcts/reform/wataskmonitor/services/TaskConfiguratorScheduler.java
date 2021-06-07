package uk.gov.hmcts.reform.wataskmonitor.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TaskConfiguratorScheduler {

    @Scheduled(fixedRate = 10_000)
    public void runTaskConfigurator() {
        log.info("Task configurator starts...");
        log.info("Task configurator ends...");
    }
}
