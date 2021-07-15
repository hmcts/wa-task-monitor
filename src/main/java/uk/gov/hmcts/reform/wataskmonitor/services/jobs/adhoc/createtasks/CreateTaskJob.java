package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CaseEventHandlerClient;
import uk.gov.hmcts.reform.wataskmonitor.models.caseeventhandler.EventInformation;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetailName;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.adhoc.createtasks.CaseIdList;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.adhoc.createtasks.CreateTaskJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.JobOutcomeService;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.JobService;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.ResourceEnum;
import uk.gov.hmcts.reform.wataskmonitor.services.utilities.ObjectMapperUtility;
import uk.gov.hmcts.reform.wataskmonitor.services.utilities.ResourceUtility;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetailName.AD_HOC_CREATE_TASKS;
import static uk.gov.hmcts.reform.wataskmonitor.services.utilities.LoggingUtility.logPrettyPrint;

@Slf4j
@Component
public class CreateTaskJob implements JobService {

    private final CaseEventHandlerClient caseEventHandlerClient;
    private final JobOutcomeService createTaskJobOutcomeService;

    public CreateTaskJob(CaseEventHandlerClient caseEventHandlerClient,
                         JobOutcomeService createTaskJobOutcomeService) {
        this.caseEventHandlerClient = caseEventHandlerClient;
        this.createTaskJobOutcomeService = createTaskJobOutcomeService;
    }

    @Override
    @SuppressWarnings("PMD.LawOfDemeter")
    public boolean canRun(JobDetailName jobDetailName) {
        return AD_HOC_CREATE_TASKS.equals(jobDetailName);
    }


    @Override
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public void run(String serviceToken) {
        log.info("Starting '{}'", AD_HOC_CREATE_TASKS);

        List<CreateTaskJobOutcome> report = new ArrayList<>();
        getCaseIdList().getCaseIds().forEach(caseId -> {
            sendMessageToInitiateTask(serviceToken, caseId);
            CreateTaskJobOutcome createTaskJobOutcome = (CreateTaskJobOutcome) createTaskJobOutcomeService
                .getJobOutcome(serviceToken, caseId);
            report.add(createTaskJobOutcome);

        });

        log.info("{} finished successfully: {}", AD_HOC_CREATE_TASKS, logPrettyPrint(report));
    }

    private CaseIdList getCaseIdList() {
        return ObjectMapperUtility
            .stringToObject(ResourceUtility.getResource(ResourceEnum.AD_HOC_CREATE_TASKS), CaseIdList.class);
    }

    private void sendMessageToInitiateTask(String serviceToken, String caseId) {
        caseEventHandlerClient.sendMessage(serviceToken,
            EventInformation.builder()
                .eventInstanceId(UUID.randomUUID().toString())
                .eventTimeStamp(LocalDateTime.now())
                .caseId(caseId)
                .jurisdictionId("ia")
                .caseTypeId("asylum")
                .eventId("buildCase")
                .newStateId("caseUnderReview")
                .userId("some user Id")
                .build());
    }

}
