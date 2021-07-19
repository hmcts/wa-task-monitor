package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CaseEventHandlerClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.caseeventhandler.EventInformation;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CreateTaskJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CreateTaskJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.ElasticSearchRetrieverParameter;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.JobOutcomeService;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.JobService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.AD_HOC_CREATE_TASKS;
import static uk.gov.hmcts.reform.wataskmonitor.utils.LoggingUtility.logPrettyPrint;

@Slf4j
@Component
public class CreateTaskJob implements JobService {

    private final CaseEventHandlerClient caseEventHandlerClient;
    private final JobOutcomeService createTaskJobOutcomeService;
    private final ElasticSearchCaseRetrieverService elasticSearchCaseRetrieverService;

    public CreateTaskJob(CaseEventHandlerClient caseEventHandlerClient,
                         JobOutcomeService createTaskJobOutcomeService,
                         ElasticSearchCaseRetrieverService
                             elasticSearchCaseRetrieverService) {
        this.caseEventHandlerClient = caseEventHandlerClient;
        this.createTaskJobOutcomeService = createTaskJobOutcomeService;
        this.elasticSearchCaseRetrieverService = elasticSearchCaseRetrieverService;
    }

    @Override
    @SuppressWarnings("PMD.LawOfDemeter")
    public boolean canRun(JobName jobName) {
        return AD_HOC_CREATE_TASKS.equals(jobName);
    }

    @Override
    public void run(String serviceToken) {
        log.info("Starting '{}'", AD_HOC_CREATE_TASKS);
        List<CreateTaskJobOutcome> outcomeList = createTasks(serviceToken);
        log.info(
            "{} finished successfully: {}",
            AD_HOC_CREATE_TASKS,
            logPrettyPrint(new CreateTaskJobReport(outcomeList))
        );
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    private List<CreateTaskJobOutcome> createTasks(String serviceToken) {
        // todo: get admin user token
        String userToken = "some user token";
        List<CreateTaskJobOutcome> outcomeList = new ArrayList<>();

        elasticSearchCaseRetrieverService.getCaseIdList(ElasticSearchRetrieverParameter.builder()
                                                            .authentication(userToken)
                                                            .serviceAuthentication(serviceToken)
                                                            .build()).getCases()
            .forEach(ccdCase -> {
                sendMessageToInitiateTask(serviceToken, ccdCase.getId());
                CreateTaskJobOutcome createTaskJobOutcome = (CreateTaskJobOutcome) createTaskJobOutcomeService
                    .getJobOutcome(serviceToken, ccdCase.getId());
                outcomeList.add(createTaskJobOutcome);

            });
        return outcomeList;
    }

    private void sendMessageToInitiateTask(String serviceToken, String caseId) {
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
