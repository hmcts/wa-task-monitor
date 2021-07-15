package uk.gov.hmcts.reform.wataskmonitor.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.utils.ResourceUtility;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static uk.gov.hmcts.reform.wataskmonitor.services.jobs.RequestsEnum.CONFIGURATION_JOB_SERVICE;
import static uk.gov.hmcts.reform.wataskmonitor.services.jobs.RequestsEnum.DELETE_PROCESS_INSTANCES_JOB_SERVICE;

@Component
@Slf4j
public class CamundaService {

    public static final String CAMUNDA_DATE_REQUEST_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS+0000";

    private final CamundaClient camundaClient;

    @Autowired
    public CamundaService(CamundaClient camundaClient) {
        this.camundaClient = camundaClient;
    }

    public List<CamundaTask> getUnConfiguredTasks(String serviceToken) {
        log.info("Retrieving unconfigured tasks from camunda.");
        List<CamundaTask> camundaTasks = camundaClient.getTasks(
            serviceToken,
            "0",
            "1000",
            buildUnconfiguredTasksRequest()
        );
        log.info("{} task(s) retrieved successfully.", camundaTasks.size());
        return camundaTasks;
    }

    public String deleteProcessInstances(String serviceToken) {
        log.info("Deleting process instances from camunda.");
        return camundaClient.deleteProcessInstance(serviceToken, buildDeleteProcessInstanceRequest());
    }

    private String buildDeleteProcessInstanceRequest() {
        return ResourceUtility.getResource(DELETE_PROCESS_INSTANCES_JOB_SERVICE.getRequestParameterBody());
    }

    private String buildUnconfiguredTasksRequest() {
        return ResourceUtility.getResource(CONFIGURATION_JOB_SERVICE.getRequestParameterBody())
            .replace("CREATED_BEFORE_PLACEHOLDER", getCreatedBeforeDate());
    }

    private static String getCreatedBeforeDate() {
        return ZonedDateTime.now()
            .minusMinutes(5)
            .format(DateTimeFormatter.ofPattern(CAMUNDA_DATE_REQUEST_PATTERN));
    }

}
