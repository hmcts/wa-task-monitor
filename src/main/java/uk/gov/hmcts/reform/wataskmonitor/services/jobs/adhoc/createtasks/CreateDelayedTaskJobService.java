package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CaseEventHandlerClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.caseeventhandler.EventInformation;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.ElasticSearchRetrieverParameter;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CreateTaskJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CreateTaskJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchCase;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchCaseList;
import uk.gov.hmcts.reform.wataskmonitor.services.JobOutcomeService;
import uk.gov.hmcts.reform.wataskmonitor.services.ResourceEnum;
import uk.gov.hmcts.reform.wataskmonitor.services.retrievecaselist.ElasticSearchCaseRetrieverService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class CreateDelayedTaskJobService {

    private final CaseEventHandlerClient caseEventHandlerClient;
    private final JobOutcomeService createTaskJobOutcomeService;
    private final ElasticSearchCaseRetrieverService retrieverService;

    public CreateDelayedTaskJobService(CaseEventHandlerClient caseEventHandlerClient,
                                       JobOutcomeService createTaskJobOutcomeService,
                                       ElasticSearchCaseRetrieverService retrieverService) {
        this.caseEventHandlerClient = caseEventHandlerClient;
        this.createTaskJobOutcomeService = createTaskJobOutcomeService;
        this.retrieverService = retrieverService;
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public CreateTaskJobReport createTasks(String serviceToken) {
        ElasticSearchCaseList searchCaseList = retrieverService.retrieveCaseList(
            new ElasticSearchRetrieverParameter(
                serviceToken,
                ResourceEnum.AD_HOC_CREATE_TASKS_CCD_ELASTIC_SEARCH_QUERY
            ));
        return new CreateTaskJobReport(
            searchCaseList.getTotal(),
            sendMessagesAndReturnOutcomesForDelayedTask(serviceToken, searchCaseList)
        );
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    private List<CreateTaskJobOutcome> sendMessagesAndReturnOutcomesForDelayedTask(String serviceToken,
                                                                                   ElasticSearchCaseList searchCaseList) {
        List<CreateTaskJobOutcome> partialOutcomeList = new ArrayList<>();
        searchCaseList.getCases()
            .forEach(ccdCase -> {
                CreateTaskJobOutcome partialOutcome = sendMessageAndReturnOutcomeForDelayedTask(serviceToken, ccdCase);
                log.info(partialOutcome.toString());
                partialOutcomeList.add(partialOutcome);
            });
        return partialOutcomeList;
    }

    private CreateTaskJobOutcome sendMessageAndReturnOutcomeForDelayedTask(String serviceToken, ElasticSearchCase ccdCase) {
        try {
            sendMessageToInitiateDelayedTask(serviceToken, ccdCase.getId());
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

    private void sendMessageToInitiateDelayedTask(String serviceToken, String caseId) {
        log.info("Sending message(caseId={}) to CEH...", caseId);
        caseEventHandlerClient.sendMessage(
            serviceToken,
            EventInformation.builder()
                .eventInstanceId(UUID.randomUUID().toString())
                .eventTimeStamp(LocalDateTime.now())
                .caseId(caseId)
                .jurisdictionId("ia")
                .caseTypeId("asylum")
                .eventId("removeRepresentation")
                .userId("some user Id")
                .build()
        );
    }
}
