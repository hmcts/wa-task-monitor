package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionTimeoutException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.clients.CaseEventHandlerClient;
import uk.gov.hmcts.reform.wataskmonitor.models.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.models.caseeventhandler.EventInformation;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetailName;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.adhoc.createtasks.CreateTaskJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.JobService;
import uk.gov.hmcts.reform.wataskmonitor.services.utilities.LoggingUtility;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetailName.AD_HOC_CREATE_TASKS;

@Slf4j
@Component
public class CreateTaskJob implements JobService {

    private final CaseEventHandlerClient caseEventHandlerClient;
    private final CamundaClient camundaClient;

    public CreateTaskJob(CaseEventHandlerClient caseEventHandlerClient,
                         CamundaClient camundaClient) {
        this.caseEventHandlerClient = caseEventHandlerClient;
        this.camundaClient = camundaClient;
    }

    @Override
    @SuppressWarnings("PMD.LawOfDemeter")
    public boolean canRun(JobDetailName jobDetailName) {
        return AD_HOC_CREATE_TASKS.equals(jobDetailName);
    }

    @Override
    public void run(String serviceToken) {
        log.info("Starting '{}'", AD_HOC_CREATE_TASKS);
        //todo: read case Ids
        String caseId = "1626277296363571";

        sendMessageToInitiateTask(serviceToken, caseId);
        CreateTaskJobOutcome createTaskJobOutcome = waitForJobOutcome(serviceToken, caseId);

        log.info("{} finished successfully: {}", AD_HOC_CREATE_TASKS, LoggingUtility.logPrettyPrint(List.of(createTaskJobOutcome)));
    }

    private CreateTaskJobOutcome waitForJobOutcome(String serviceToken, String caseId) {
        try {
            return await()
                .pollInterval(5, SECONDS)
                .atMost(15, SECONDS)
                .until(() -> checkTaskWasCreatedSuccessfully(serviceToken, caseId), CreateTaskJobOutcome::isCreated);
        } catch (ConditionTimeoutException e) {
            return CreateTaskJobOutcome.builder()
                .caseId(caseId)
                .created(false)
                .build();
        }
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

    private CreateTaskJobOutcome checkTaskWasCreatedSuccessfully(String serviceToken, String caseId) {
        List<CamundaTask> camundaTaskList = camundaClient.getTasksByTaskVariables(serviceToken,
            "caseId_eq_" + caseId + ",taskType_eq_reviewAppealSkeletonArgument",
            "created",
            "desc");

        if (!camundaTaskList.isEmpty() && camundaTaskList.get(0).getName().equals("Review Appeal Skeleton Argument")) {
            return CreateTaskJobOutcome.builder()
                .taskId(camundaTaskList.get(0).getId())
                .processInstanceId(camundaTaskList.get(0).getProcessInstanceId())
                .caseId(caseId)
                .created(true)
                .build();
        }
        return CreateTaskJobOutcome.builder()
            .caseId(caseId)
            .created(false)
            .build();
    }

}
