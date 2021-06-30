package uk.gov.hmcts.reform.wataskmonitor.services.jobs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetailName;
import uk.gov.hmcts.reform.wataskmonitor.services.camunda.CamundaService;

@Slf4j
@Component
public class DeleteProcessInstancesJob implements JobService {

    public static final String DELETE_PROCESS_INSTANCE_JOB = "Delete process instance job";
    private final AuthTokenGenerator authTokenGenerator;
    private final CamundaService camundaService;

    public DeleteProcessInstancesJob(AuthTokenGenerator authTokenGenerator, CamundaService camundaService) {
        this.authTokenGenerator = authTokenGenerator;
        this.camundaService = camundaService;
    }

    @Override
    @SuppressWarnings("PMD.LawOfDemeter")
    public boolean canRun(JobDetailName jobDetailName) {
        return JobDetailName.AD_HOC.equals(jobDetailName);
    }

    @Override
    public void run() {
        log.info("Starting '" + DELETE_PROCESS_INSTANCE_JOB);
        String serviceToken = authTokenGenerator.generate();
        String response = camundaService.deleteProcessInstances(serviceToken);
        log.info("{} finished successfully: {}", DELETE_PROCESS_INSTANCE_JOB, response);
    }
}
