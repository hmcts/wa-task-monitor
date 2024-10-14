package uk.gov.hmcts.reform.wataskmonitor.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.MonitorTaskJobRequest;
import uk.gov.hmcts.reform.wataskmonitor.services.controllers.MonitorTaskJobService;

import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
public class MonitorTaskJobController {

    private final MonitorTaskJobService monitorTaskJobService;

    @Autowired
    public MonitorTaskJobController(MonitorTaskJobService monitorTaskJobService) {
        this.monitorTaskJobService = monitorTaskJobService;
    }

    @PostMapping("/monitor/tasks/jobs")
    public ResponseEntity<MonitorTaskJobRequest> monitorTaskJob(@RequestBody MonitorTaskJobRequest monitorTaskJobReq) {
        log.info("Received request to create a new job of type '{}'", monitorTaskJobReq.getJobDetails().getName());
        try {
            CompletableFuture<String> future =
                monitorTaskJobService.execute(monitorTaskJobReq.getJobDetails().getName());
            String result = future.join();
            log.info("Job '{}' processed in the background with result {}",
                     monitorTaskJobReq.getJobDetails().getName(), result);
            return new ResponseEntity<>(monitorTaskJobReq, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing job monitorTaskJob '{}'",
                      monitorTaskJobReq.getJobDetails().getName());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while running job "
                + monitorTaskJobReq.getJobDetails().getName(), e);
        }
    }

}
