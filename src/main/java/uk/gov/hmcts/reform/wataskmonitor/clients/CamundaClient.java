package uk.gov.hmcts.reform.wataskmonitor.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wataskmonitor.config.SecurityConfiguration.SERVICE_AUTHORIZATION;

@FeignClient(
    name = "camunda",
    url = "${camunda.url}"
)
@SuppressWarnings("PMD.UseObjectForClearerAPI")
public interface CamundaClient {

    @PostMapping(value = "/task",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    List<CamundaTask> getTasks(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @RequestParam(value = "firstResult", required = false, defaultValue = "0") String firstResult,
        @RequestParam(value = "maxResults", required = false, defaultValue = "1000") String maxResults,
        @RequestBody String body
    );


    @PostMapping(value = "/process-instance/delete",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    String deleteProcessInstance(@RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                                 @RequestBody String body);


    @PostMapping(value = "/history/task",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    List<HistoricCamundaTask> getTasksFromHistory(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @RequestParam(value = "firstResult", required = false, defaultValue = "0") String firstResult,
        @RequestParam(value = "maxResults", required = false, defaultValue = "1000") String maxResults,
        @RequestBody String body
    );
}

