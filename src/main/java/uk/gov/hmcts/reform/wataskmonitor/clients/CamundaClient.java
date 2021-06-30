package uk.gov.hmcts.reform.wataskmonitor.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.wataskmonitor.models.camunda.CamundaTask;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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
    List<CamundaTask> getTasks(@RequestHeader("ServiceAuthorization") String serviceAuthorisation,
                               @RequestParam(value = "firstResult", required = false, defaultValue = "0")
                                   String firstResult,
                               @RequestParam(value = "maxResults", required = false, defaultValue = "1000")
                                   String maxResults,
                               @RequestBody String body);

}

