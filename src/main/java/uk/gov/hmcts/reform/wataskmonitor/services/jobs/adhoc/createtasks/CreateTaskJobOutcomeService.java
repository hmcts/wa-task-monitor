package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks;

import org.awaitility.core.ConditionTimeoutException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.adhoc.createtasks.CreateTaskJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.JobOutcomeService;

import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

@Component
public class CreateTaskJobOutcomeService implements JobOutcomeService {

    private final CamundaClient camundaClient;

    public CreateTaskJobOutcomeService(CamundaClient camundaClient) {
        this.camundaClient = camundaClient;
    }

    @Override
    public CreateTaskJobOutcome getJobOutcome(String serviceToken, String caseId) {
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

    private CreateTaskJobOutcome checkTaskWasCreatedSuccessfully(String serviceToken, String caseId) {
        List<CamundaTask> camundaTaskList = camundaClient.getTasksByTaskVariables(
            serviceToken,
            "caseId_eq_" + caseId + ",taskType_eq_reviewAppealSkeletonArgument",
            "created",
            "desc"
        );

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
