package uk.gov.hmcts.reform.wataskmonitor.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.wataskmonitor.config.CamelCaseFeignConfiguration;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTaskCount;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaVariable;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoryVariableInstance;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wataskmonitor.config.SecurityConfiguration.SERVICE_AUTHORIZATION;

@FeignClient(
    name = "camunda",
    url = "${camunda.url}",
    configuration = CamelCaseFeignConfiguration.class
)
@SuppressWarnings({"PMD.UseObjectForClearerAPI", "PMD.AvoidDuplicateLiterals"})
public interface CamundaClient {

    @PostMapping(
        value = "/task",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    List<CamundaTask> getTasks(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @RequestParam(value = "firstResult", required = false, defaultValue = "0") String firstResult,
        @RequestParam(value = "maxResults", required = false, defaultValue = "100") String maxResults,
        @RequestBody String body
    );


    @PostMapping(
        value = "/process-instance/delete",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    String deleteProcessInstance(@RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                                 @RequestBody String body);

    @GetMapping(
        value = "/task",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    List<CamundaTask> getTasksByTaskVariables(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @RequestParam("taskVariables") String taskVariables,
        @RequestParam(value = "sortBy", defaultValue = "created", required = false) String sortBy,
        @RequestParam(value = "sortOrder", defaultValue = "desc", required = false) String sortOrder
    );

    @PostMapping(
        value = "/history/task",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    List<HistoricCamundaTask> getTasksFromHistory(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @RequestParam(value = "firstResult", required = false, defaultValue = "0") String firstResult,
        @RequestParam(value = "maxResults", required = false, defaultValue = "100") String maxResults,
        @RequestBody String body
    );

    @GetMapping(
        value = "/task/{task-id}/variables",
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    Map<String, CamundaVariable> getVariables(@RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                                              @PathVariable("task-id") String id);

    @DeleteMapping(
        value = "/history/variable-instance/{variable-instance-id}",
        consumes = APPLICATION_JSON_VALUE
    )
    void deleteVariableFromHistory(@RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                                   @PathVariable("variable-instance-id") String variableInstanceId);


    @PostMapping(
        value = "/history/variable-instance",
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE
    )
    List<HistoryVariableInstance> searchHistory(@RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                                                @RequestBody Map<String, Object> body);

    @GetMapping(
        value = "/process-instance/count",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    CamundaTaskCount getActiveProcessCount(
        @RequestParam("startedBefore") String startedBefore
    );

    @PostMapping(
        value = "/process-instance/delete",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    void deleteActiveProcesses(@RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                               @RequestBody String body);

    @PostMapping(
        value = "/history/process-instance",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    List<HistoricCamundaTask> getHistoryProcesses(
        @RequestParam(value = "firstResult", required = false, defaultValue = "0") String firstResult,
        @RequestParam(value = "maxResults", required = false, defaultValue = "100") String maxResults,
        @RequestBody String body
    );

    @GetMapping(
        value = "/history/process-instance/count",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    CamundaTaskCount getHistoryProcessCount(
        @RequestParam("startedBefore") String startedBefore
    );

    @PostMapping(
        value = "/history/process-instance/delete",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    void deleteHistoryProcesses(@RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                                @RequestBody String body);

}

