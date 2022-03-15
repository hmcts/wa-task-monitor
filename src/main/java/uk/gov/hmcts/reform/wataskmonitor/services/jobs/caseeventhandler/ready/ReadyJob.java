package uk.gov.hmcts.reform.wataskmonitor.services.jobs.caseeventhandler.ready;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.caseeventhandler.MessageJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;

import java.util.List;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.READY;
import static uk.gov.hmcts.reform.wataskmonitor.utils.LoggingUtility.logPrettyPrint;


@Slf4j
@Component
public class ReadyJob implements JobService {
    private final ReadyJobService readyJobService;

    public ReadyJob(ReadyJobService readyJobService) {
        this.readyJobService = readyJobService;
    }

    @Override
    public boolean canRun(JobName jobName) {
        return READY.equals(jobName);
    }

    @Override
    public void run(String serviceToken) {
        log.info("Starting task {} job.", READY);
        try {
            final List<String> readyMessages = readyJobService.getReadyMessages(serviceToken, READY);
            if (readyMessages.isEmpty()) {
                log.info("there were no ready messages");
            } else {
                MessageJobReport messageJobReport = new MessageJobReport(readyMessages.size(), readyMessages);
                log.info("{} job finished successfully: {}", READY, logPrettyPrint(messageJobReport));
            }
        } catch (Exception e) {
            log.error("Error while retrieving ready messages");
        }
    }
}
