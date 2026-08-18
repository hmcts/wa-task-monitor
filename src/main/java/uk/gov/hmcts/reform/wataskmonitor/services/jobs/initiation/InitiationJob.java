package uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.config.LaunchDarklyFeatureFlagProvider;
import uk.gov.hmcts.reform.wataskmonitor.config.features.FeatureFlag;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.JobService;

import java.util.List;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.INITIATION;
import static uk.gov.hmcts.reform.wataskmonitor.utils.LoggingUtility.logPrettyPrint;

@Slf4j
@Component
public class InitiationJob implements JobService {
    private static final String JOB_TYPE = "Task Initiation";

    private final CamundaService camundaService;
    private final InitiationService initiationService;
    private final LaunchDarklyFeatureFlagProvider launchDarklyFeatureFlagProvider;

    @Autowired
    public InitiationJob(CamundaService camundaService,
                         InitiationService initiationService,
                         LaunchDarklyFeatureFlagProvider launchDarklyFeatureFlagProvider) {
        this.camundaService = camundaService;
        this.initiationService = initiationService;
        this.launchDarklyFeatureFlagProvider = launchDarklyFeatureFlagProvider;
    }

    @Override
    public boolean canRun(JobName jobName) {
        return INITIATION.equals(jobName);
    }

    @Override
    public void run(String serviceToken) {
        if (launchDarklyFeatureFlagProvider.getBooleanValue(FeatureFlag.WA_INITIATE_TASKS_ON_CREATE)) {
            log.info("{} job skipped because {} feature flag is enabled.",
                     INITIATION,
                     FeatureFlag.WA_INITIATE_TASKS_ON_CREATE.getKey());
            return;
        }

        log.info("Starting task {} job.", INITIATION);
        List<CamundaTask> tasks = camundaService.getInitiationCandidates(serviceToken);
        GenericJobReport report = initiationService.initiateTasks(tasks, serviceToken, JOB_TYPE);
        log.info("{} job finished successfully: {}", INITIATION, logPrettyPrint(report));
    }
}
