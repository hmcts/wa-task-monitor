package uk.gov.hmcts.reform.wataskmonitor.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.TerminateTaskRequest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wataskmonitor.config.SecurityConfiguration.SERVICE_AUTHORIZATION;

@FeignClient(
    name = "taskManagement",
    url = "${task-management.url}"
)
public interface TaskManagementClient {

    @DeleteMapping(value = "/task/{task-id}",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    void terminateTask(@RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                       @PathVariable("task-id") String taskId,
                       @RequestBody TerminateTaskRequest body);

}
