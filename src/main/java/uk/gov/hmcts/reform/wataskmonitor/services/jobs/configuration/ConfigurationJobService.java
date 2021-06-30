package uk.gov.hmcts.reform.wataskmonitor.services.jobs.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.models.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.ResourceUtility;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class ConfigurationJobService {

    public static final String QUERY_PARAMETERS_JSON = "camunda/camunda-query-parameters.json";
    public static final String CAMUNDA_DATE_REQUEST_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS+0000";

    private final CamundaClient camundaClient;

    @Autowired
    public ConfigurationJobService(CamundaClient camundaClient) {
        this.camundaClient = camundaClient;
    }

    public List<CamundaTask> getUnConfiguredTasks(String serviceToken) {
        log.info("Retrieving unconfigured tasks from camunda.");
        List<CamundaTask> camundaTasks = camundaClient.getTasks(
            serviceToken,
            "0",
            "1000",
            getQueryParameters()
        );
        log.info("{} task(s) retrieved successfully.", camundaTasks.size());
        return camundaTasks;
    }

    private String getQueryParameters() {
        return ResourceUtility.getResource(QUERY_PARAMETERS_JSON)
            .replace("CREATED_BEFORE_PLACEHOLDER", getCreatedBeforeDate());
    }

    private static String getCreatedBeforeDate() {
        return ZonedDateTime.now()
            .minusMinutes(5)
            .format(DateTimeFormatter.ofPattern(CAMUNDA_DATE_REQUEST_PATTERN));
    }

}
