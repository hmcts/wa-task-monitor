package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.ResourceUtility;

@Component
@Slf4j
public class DeleteProcessInstancesJobService {

    public static final String PROCESS_INSTANCES_REQUEST_PARAMETER_JSON =
        "camunda/camunda-delete-process-instances-request-parameter.json";

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
        return ResourceUtility.getResource(PROCESS_INSTANCES_REQUEST_PARAMETER_JSON);
    }


}
