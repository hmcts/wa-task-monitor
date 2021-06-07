package uk.gov.hmcts.reform.wataskmonitor.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.wataskmonitor.models.Task;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(
    name = "camunda",
    url = "${camunda.url}"
)
public interface CamundaClient {

    @PostMapping(value = "/task",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    List<Task> getTasks(@RequestHeader("ServiceAuthorization") String serviceAuthorisation,
                        @RequestParam(value = "firstResult", defaultValue = "0") Integer firstResult,
                        @RequestParam(value = "maxResults", defaultValue = "0") Integer maxResults,
                        @RequestBody Map<String, Object> body);

}

