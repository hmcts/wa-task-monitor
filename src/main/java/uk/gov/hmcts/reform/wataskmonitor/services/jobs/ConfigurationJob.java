package uk.gov.hmcts.reform.wataskmonitor.services.jobs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.models.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetailName;
import uk.gov.hmcts.reform.wataskmonitor.services.camunda.CamundaService;
import uk.gov.hmcts.reform.wataskmonitor.services.taskconfiguration.TaskConfigurationService;

import java.util.List;

@Slf4j
@Component
public class ConfigurationJob implements JobService {
    public static final String TASK_CONFIGURATION_JOB = "task configuration job";
    private final CamundaService camundaService;
    private final TaskConfigurationService taskConfigurationService;
    private final AuthTokenGenerator authTokenGenerator;

    public ConfigurationJob(CamundaService camundaService,
                            TaskConfigurationService taskConfigurationService,
                            AuthTokenGenerator authTokenGenerator) {
        this.camundaService = camundaService;
        this.taskConfigurationService = taskConfigurationService;
        this.authTokenGenerator = authTokenGenerator;
    }

    @Override
    @SuppressWarnings("PMD.LawOfDemeter")
    public boolean canRun(JobDetailName jobDetailName) {
        return JobDetailName.CONFIGURATION.equals(jobDetailName);
    }

    @Override
    public void run() {
        log.info("Starting " + TASK_CONFIGURATION_JOB + ".");
        String serviceToken = authTokenGenerator.generate();
        List<CamundaTask> tasks = camundaService.getUnConfiguredTasks(serviceToken);
        taskConfigurationService.configureTasks(tasks, serviceToken);
        log.info(TASK_CONFIGURATION_JOB + " finished successfully.");
    }
}
