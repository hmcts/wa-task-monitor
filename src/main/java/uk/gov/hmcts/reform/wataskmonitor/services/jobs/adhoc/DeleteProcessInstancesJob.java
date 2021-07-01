package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetailName;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.JobService;

import static uk.gov.hmcts.reform.wataskmonitor.services.jobs.utilities.LoggingUtility.logPrettyPrint;

@Slf4j
@Component
public class DeleteProcessInstancesJob implements JobService {

    public static final String DELETE_PROCESS_INSTANCE_JOB = "Delete process instance job";
    private final AuthTokenGenerator authTokenGenerator;
    private final DeleteProcessInstancesJobService deleteProcessInstancesJobService;

    public DeleteProcessInstancesJob(AuthTokenGenerator authTokenGenerator,
                                     DeleteProcessInstancesJobService deleteProcessInstancesJobService) {
        this.authTokenGenerator = authTokenGenerator;
        this.deleteProcessInstancesJobService = deleteProcessInstancesJobService;
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
        String response = deleteProcessInstancesJobService.deleteProcessInstances(serviceToken);
        log.info("{} finished successfully: {}", DELETE_PROCESS_INSTANCE_JOB, logPrettyPrint.apply(response));
    }

}
