package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CreateTaskJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.services.JobOutcomeService;

import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

@Component
@Slf4j
public class CreateTaskJobOutcomeService implements JobOutcomeService {

    @Value("${job.configuration.outcome-service-timeout}")
    private Integer timeout;

    @Value("${job.configuration.outcome-service-pollInterval}")
    private Integer pollInterval;

    private final CamundaClient camundaClient;

    public CreateTaskJobOutcomeService(CamundaClient camundaClient) {
        this.camundaClient = camundaClient;
    }

    @Override
    public CreateTaskJobOutcome getJobOutcome(String serviceToken, String caseId) {
        log.info("Getting job outcome...");
        try {
            return await()
                .ignoreExceptions()
                .pollInterval(pollInterval, SECONDS)
                .atMost(timeout, SECONDS)
                .until(() -> checkTaskWasCreatedSuccessfully(serviceToken, caseId), CreateTaskJobOutcome::isCreated);
        } catch (Exception e) {
            return CreateTaskJobOutcome.builder()
                .caseId(caseId)
                .created(false)
                .build();
        }
    }

    private CreateTaskJobOutcome checkTaskWasCreatedSuccessfully(String serviceToken, String caseId) {
        List<CamundaTask> camundaTaskList = camundaClient.getTasksByTaskVariables(
            serviceToken,
            "caseId_eq_" + caseId + ",taskType_eq_reviewAppealSkeletonArgument",
            "created",
            "desc"
        );

        if (camundaTaskList != null && !camundaTaskList.isEmpty()
            && camundaTaskList.get(0).getName().equals("Review Appeal Skeleton Argument")) {
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
