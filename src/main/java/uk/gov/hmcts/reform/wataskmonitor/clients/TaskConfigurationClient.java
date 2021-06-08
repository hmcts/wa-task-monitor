package uk.gov.hmcts.reform.wataskmonitor.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(
    name = "taskConfiguration",
    url = "${task.configuration.service.url}"
)
public interface TaskConfigurationClient {

    @PostMapping(value = "/task/{task-id}",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    String configureTask(@RequestHeader("ServiceAuthorization") String serviceAuthorisation,
                         @PathVariable("task-id") String taskId);

}
