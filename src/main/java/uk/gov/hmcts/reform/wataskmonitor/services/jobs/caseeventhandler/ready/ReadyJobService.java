package uk.gov.hmcts.reform.wataskmonitor.services.jobs.caseeventhandler.ready;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CaseEventHandlerClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

import java.util.List;

@Component
@Slf4j
public class ReadyJobService {
    private final CaseEventHandlerClient caseEventHandlerClient;

    public ReadyJobService(CaseEventHandlerClient caseEventHandlerClient) {
        this.caseEventHandlerClient = caseEventHandlerClient;
    }

    public List<String> getReadyMessages(String serviceToken, JobName jobName) {
        log.info("Retrieving messages of type '{}' from case db", jobName.name());
        return caseEventHandlerClient.findProblematicMessages(serviceToken, jobName.name());
    }
}
