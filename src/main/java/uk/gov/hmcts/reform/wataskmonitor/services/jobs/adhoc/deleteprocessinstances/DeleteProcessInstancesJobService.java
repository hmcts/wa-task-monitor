package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.deleteprocessinstances;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.services.utilities.ResourceUtility;

import static uk.gov.hmcts.reform.wataskmonitor.services.jobs.RequestParameterEnum.DELETE_PROCESS_INSTANCES_JOB_SERVICE;

@Component
@Slf4j
public class DeleteProcessInstancesJobService {

    private final CamundaClient camundaClient;

    @Autowired
    public DeleteProcessInstancesJobService(CamundaClient camundaClient) {
        this.camundaClient = camundaClient;
    }

    public String deleteProcessInstances(String serviceToken) {
        log.info("Deleting process instances from camunda.");
        return camundaClient.deleteProcessInstance(serviceToken, getRequestParameter());
    }

    private String getRequestParameter() {
        return ResourceUtility.getResource(DELETE_PROCESS_INSTANCES_JOB_SERVICE.getRequestParameterBody());
    }

}
