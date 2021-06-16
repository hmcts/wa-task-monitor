package uk.gov.hmcts.reform.wataskmonitor.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.wataskmonitor.models.MonitorTaskJobReq;

@RestController
@Slf4j
public class MonitorTaskJobController {

    @PostMapping("/monitor/tasks/jobs")
    public MonitorTaskJobReq monitorTaskJob(@RequestBody MonitorTaskJobReq monitorTaskJob) {
        log.info(monitorTaskJob.toString());
        return monitorTaskJob;
    }
}
