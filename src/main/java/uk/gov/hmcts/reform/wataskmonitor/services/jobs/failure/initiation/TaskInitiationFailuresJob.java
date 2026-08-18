package uk.gov.hmcts.reform.wataskmonitor.services.jobs.failure.initiation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.config.LaunchDarklyFeatureFlagProvider;
import uk.gov.hmcts.reform.wataskmonitor.config.features.FeatureFlag;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation.CamundaService;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation.InitiationService;

import java.util.List;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.TASK_INITIATION_FAILURES;
import static uk.gov.hmcts.reform.wataskmonitor.utils.LoggingUtility.logPrettyPrint;

@Slf4j
@Component
public class TaskInitiationFailuresJob implements JobService {
    private final TaskInitiationFailuresLogService taskInitiationFailuresLogService;
    private final CamundaService camundaService;
    private final InitiationService initiationService;
    private final LaunchDarklyFeatureFlagProvider launchDarklyFeatureFlagProvider;

    @Autowired
    public TaskInitiationFailuresJob(TaskInitiationFailuresLogService taskInitiationFailuresLogService,
                                     CamundaService camundaService,
                                     InitiationService initiationService,
                                     LaunchDarklyFeatureFlagProvider launchDarklyFeatureFlagProvider) {
        this.taskInitiationFailuresLogService = taskInitiationFailuresLogService;
        this.camundaService = camundaService;
        this.initiationService = initiationService;
        this.launchDarklyFeatureFlagProvider = launchDarklyFeatureFlagProvider;
    }

    @Override
    public boolean canRun(JobName jobName) {
        return TASK_INITIATION_FAILURES.equals(jobName);
    }

    @Override
    public void run(String serviceToken) {
        log.info("Starting task {} job.", TASK_INITIATION_FAILURES);
        GenericJobReport report;
        if (launchDarklyFeatureFlagProvider.getBooleanValue(FeatureFlag.WA_INITIATE_TASKS_ON_CREATE)) {
            List<CamundaTask> tasks = camundaService.getUnconfiguredTasks(serviceToken);
            report = initiationService.initiateTasks(
                tasks,
                serviceToken,
                TASK_INITIATION_FAILURES.name()
            );
        } else {
            List<CamundaTask> tasks = camundaService.getStaleUnconfiguredTasks(serviceToken);
            report = taskInitiationFailuresLogService.reportInitiationFailures(tasks, serviceToken);
        }
        log.info("{} job completed successfully: {}", TASK_INITIATION_FAILURES, logPrettyPrint(report));
    }
}
