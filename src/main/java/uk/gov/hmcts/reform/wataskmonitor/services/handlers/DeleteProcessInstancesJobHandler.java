package uk.gov.hmcts.reform.wataskmonitor.services.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.services.CamundaService;

import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.AD_HOC_DELETE_PROCESS_INSTANCES;
import static uk.gov.hmcts.reform.wataskmonitor.utils.LoggingUtility.logPrettyPrint;

@Slf4j
@Component
public class DeleteProcessInstancesJobHandler implements JobHandler {

    private final AuthTokenGenerator authTokenGenerator;
    private final CamundaService camundaService;

    public DeleteProcessInstancesJobHandler(AuthTokenGenerator authTokenGenerator,
                                            CamundaService camundaService) {
        this.authTokenGenerator = authTokenGenerator;
        this.camundaService = camundaService;
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
        String response = camundaService.deleteProcessInstances(serviceToken);
        log.info("{} finished successfully: {}", AD_HOC_DELETE_PROCESS_INSTANCES, logPrettyPrint.apply(response));
    }

}
