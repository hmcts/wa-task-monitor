package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CaseEventHandlerClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.caseeventhandler.EventInformation;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CreateTaskJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CreateTaskJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchCase;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchCaseList;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchRetrieverParameter;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.JobOutcomeService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class CreateTaskJobService {

    private final CaseEventHandlerClient caseEventHandlerClient;
    private final JobOutcomeService createTaskJobOutcomeService;
    private final ElasticSearchCaseRetrieverService retrieverService;

    public CreateTaskJobService(CaseEventHandlerClient caseEventHandlerClient,
                                JobOutcomeService createTaskJobOutcomeService,
                                ElasticSearchCaseRetrieverService retrieverService) {
        this.caseEventHandlerClient = caseEventHandlerClient;
        this.createTaskJobOutcomeService = createTaskJobOutcomeService;
        this.retrieverService = retrieverService;
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public CreateTaskJobReport createTasks(String serviceToken) {
        ElasticSearchCaseList searchCaseList =
            retrieverService.retrieveCaseList(new ElasticSearchRetrieverParameter(serviceToken));
        return new CreateTaskJobReport(
            searchCaseList.getTotal(),
            sendMessagesAndReturnOutcomes(serviceToken, searchCaseList)
        );
    }

    private List<CreateTaskJobOutcome> sendMessagesAndReturnOutcomes(String serviceToken,
                                                                     ElasticSearchCaseList searchCaseList) {
        List<CreateTaskJobOutcome> partialOutcomeList = new ArrayList<>();
        searchCaseList.getCases()
            .forEach(ccdCase -> {
                CreateTaskJobOutcome partialOutcome = sendMessageAndReturnOutcome(serviceToken, ccdCase);
                log.info(partialOutcome.toString());
                partialOutcomeList.add(partialOutcome);
            });
        return partialOutcomeList;
    }

    private CreateTaskJobOutcome sendMessageAndReturnOutcome(String serviceToken, ElasticSearchCase ccdCase) {
        try {
            sendMessageToInitiateTask(serviceToken, ccdCase.getId());
        } catch (Exception e) {
            log.info("Error when sending message to CEH for caseId({}), we carry on...", ccdCase.getId());
            return CreateTaskJobOutcome.builder()
                .caseId(ccdCase.getId())
                .created(false)
                .build();
        }
        return (CreateTaskJobOutcome) createTaskJobOutcomeService.getJobOutcome(
            serviceToken,
            ccdCase.getId()
        );
    }

    private void sendMessageToInitiateTask(String serviceToken, String caseId) {
        log.info("Sending message(caseId={}) to CEH...", caseId);
        caseEventHandlerClient.sendMessage(
            serviceToken,
            EventInformation.builder()
                .eventInstanceId(UUID.randomUUID().toString())
                .eventTimeStamp(LocalDateTime.now())
                .caseId(caseId)
                .jurisdictionId("ia")
                .caseTypeId("asylum")
                .eventId("buildCase")
                .newStateId("caseUnderReview")
                .userId("some user Id")
                .build()
        );
    }
}
