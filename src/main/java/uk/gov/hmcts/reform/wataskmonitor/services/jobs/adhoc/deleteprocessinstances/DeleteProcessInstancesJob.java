package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.deleteprocessinstances;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetailName;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.JobService;

import static uk.gov.hmcts.reform.wataskmonitor.models.jobs.JobDetailName.AD_HOC_DELETE_PROCESS_INSTANCES;
import static uk.gov.hmcts.reform.wataskmonitor.services.utilities.LoggingUtility.logPrettyPrint;

@Slf4j
@Component
public class DeleteProcessInstancesJob implements JobService {

    private final DeleteProcessInstancesJobService deleteProcessInstancesJobService;

    public DeleteProcessInstancesJob(DeleteProcessInstancesJobService deleteProcessInstancesJobService) {
        this.deleteProcessInstancesJobService = deleteProcessInstancesJobService;
    }

    @Override
    @SuppressWarnings("PMD.LawOfDemeter")
    public boolean canRun(JobDetailName jobDetailName) {
        return AD_HOC_DELETE_PROCESS_INSTANCES.equals(jobDetailName);
    }

    @Override
    public void run(String serviceToken) {
        log.info("Starting '{}'", AD_HOC_DELETE_PROCESS_INSTANCES);
        String response = deleteProcessInstancesJobService.deleteProcessInstances(serviceToken);
        log.info("{} finished successfully: {}", AD_HOC_DELETE_PROCESS_INSTANCES, logPrettyPrint.apply(response));
    }

}
