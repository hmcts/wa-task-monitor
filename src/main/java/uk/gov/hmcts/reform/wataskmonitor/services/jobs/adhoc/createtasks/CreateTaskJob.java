package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.clients.CaseEventHandlerClient;
import uk.gov.hmcts.reform.wataskmonitor.models.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.models.caseeventhandler.EventInformation;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetailName;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.JobService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
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

    @SneakyThrows
    @Override
    public void run(String serviceToken) {
        log.info("Starting '{}'", AD_HOC_CREATE_TASKS);
        //todo: read case Ids
        String caseId = "1626216741759512";

        sendMessageToInitiateTask(serviceToken, caseId);
        waitForOperationOutcome(serviceToken, caseId);

    }

    private void waitForOperationOutcome(String serviceToken, String caseId) {
        await()
            .pollInterval(5, SECONDS)
            .atMost(15, SECONDS)
            .until(
                () -> checkTaskWasCreatedSuccessfully(serviceToken, caseId), equalTo(true));
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

    private boolean checkTaskWasCreatedSuccessfully(String serviceToken, String caseId) {
        boolean result;
        CamundaTask camundaTask = camundaClient.getTasksByTaskVariables(serviceToken,
            "caseId_eq_" + caseId + ",taskType_eq_reviewAppealSkeletonArgument",
            "created",
            "desc").get(0);

        List<CamundaTask> taskCreated = new ArrayList<>();
        List<String> taskNotCreated = new ArrayList<>();
        if (camundaTask != null && camundaTask.getName().equals("Review Appeal Skeleton Argument")) {
            taskCreated.add(camundaTask);
            result = true;
        } else {
            taskNotCreated.add(caseId);
            result = false;
        }

        // print report
        log.info("{} finished successfully: "
                 + "\n number of tasks created: {} "
                 + "\n tasks created list: {} "
                 + "\n number of tasks not created: {} "
                 + "\n tasks not created for following case id List: {}",
            AD_HOC_CREATE_TASKS,
            taskCreated.size(),
            taskCreated,
            taskNotCreated.size(),
            taskNotCreated);
        return result;
    }

}
