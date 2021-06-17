package uk.gov.hmcts.reform.wataskmonitor.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.exceptions.CamundaRequestFailure;
import uk.gov.hmcts.reform.wataskmonitor.models.CamundaTask;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class CamundaService {

    public static final String QUERY_PARAMETERS_JSON = "camundaquery/camunda-query-parameters.json";
    public static final String CAMUNDA_DATE_REQUEST_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS+0000";

    private final CamundaClient camundaClient;

    @Autowired
    public CamundaService(CamundaClient camundaClient) {
        this.camundaClient = camundaClient;
    }

    public List<CamundaTask> getUnConfiguredTasks(String serviceToken) {
        log.info("Retrieving unconfigured camunda tasks...");
        List<CamundaTask> camundaTasks = camundaClient.getTasks(
            serviceToken,
            "0",
            "1000",
            getQueryParameters()
        );
        log.info("unconfigured camundaTasks retrieved successfully.");
        log.debug(camundaTasks.toString());
        return camundaTasks;
    }

    private String getQueryParameters() {
        try (var is = new ClassPathResource(QUERY_PARAMETERS_JSON).getInputStream()) {
            return FileCopyUtils.copyToString(new InputStreamReader(is, StandardCharsets.UTF_8))
                .replace("CREATED_BEFORE_PLACEHOLDER", getCreatedBeforeDate());
        } catch (IOException e) {
            throw new CamundaRequestFailure(
                "Error loading the query parameters file: " + QUERY_PARAMETERS_JSON,
                e
            );
        }
    }

    private String getCreatedBeforeDate() {
        return ZonedDateTime.now()
            .minusMinutes(5)
            .format(DateTimeFormatter.ofPattern(CAMUNDA_DATE_REQUEST_PATTERN));
    }

}
