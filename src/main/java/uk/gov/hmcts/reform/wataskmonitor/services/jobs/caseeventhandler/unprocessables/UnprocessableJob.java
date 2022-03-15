package uk.gov.hmcts.reform.wataskmonitor.services.jobs.caseeventhandler.unprocessables;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.caseeventhandler.MessageJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;

import java.util.List;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.UNPROCESSABLE;
import static uk.gov.hmcts.reform.wataskmonitor.utils.LoggingUtility.logPrettyPrint;


@Slf4j
@Component
public class UnprocessableJob implements JobService {
    private final UnprocessableJobService unprocessableJobService;

    public UnprocessableJob(UnprocessableJobService unprocessableJobService) {
        this.unprocessableJobService = unprocessableJobService;
    }

    @Override
    public boolean canRun(JobName jobName) {
        return UNPROCESSABLE.equals(jobName);
    }

    @Override
    public void run(String serviceToken) {
        log.info("Starting task {} job.", UNPROCESSABLE);
        try {
            final List<String> unprocessableMessages = unprocessableJobService.getUnprocessableMessages(serviceToken,
                                                                                                        UNPROCESSABLE);
            if (unprocessableMessages.isEmpty()) {
                log.info("there were no unprocessable messages");
            } else {
                MessageJobReport report = new MessageJobReport(unprocessableMessages.size(), unprocessableMessages);
                log.info("{} job finished successfully: {}", UNPROCESSABLE, logPrettyPrint(report));
            }
        } catch (Exception e) {
            log.error("Error while retrieving unprocessable messages");
        }
    }
}
