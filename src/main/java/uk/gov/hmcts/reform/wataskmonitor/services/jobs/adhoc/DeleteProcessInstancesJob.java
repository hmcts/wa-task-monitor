package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.JobService;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.AD_HOC_DELETE_PROCESS_INSTANCES;
import static uk.gov.hmcts.reform.wataskmonitor.utils.LoggingUtility.logPrettyPrint;

@Slf4j
@Component
public class DeleteProcessInstancesJob implements JobService {

    private final AuthTokenGenerator authTokenGenerator;
    private final DeleteProcessInstancesJobService deleteProcessInstancesJobService;

    public DeleteProcessInstancesJob(AuthTokenGenerator authTokenGenerator,
                                     DeleteProcessInstancesJobService deleteProcessInstancesJobService) {
        this.authTokenGenerator = authTokenGenerator;
        this.deleteProcessInstancesJobService = deleteProcessInstancesJobService;
    }

    @Override
    @SuppressWarnings("PMD.LawOfDemeter")
    public boolean canHandle(JobName jobName) {
        return AD_HOC_DELETE_PROCESS_INSTANCES.equals(jobName);
    }

    @Override
    public void run() {
        log.info("Starting '{}'", AD_HOC_DELETE_PROCESS_INSTANCES);
        String serviceToken = authTokenGenerator.generate();
        String response = deleteProcessInstancesJobService.deleteProcessInstances(serviceToken);
        log.info("{} finished successfully: {}", AD_HOC_DELETE_PROCESS_INSTANCES, logPrettyPrint.apply(response));
    }

}
