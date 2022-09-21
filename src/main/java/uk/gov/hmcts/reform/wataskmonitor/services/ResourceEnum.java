package uk.gov.hmcts.reform.wataskmonitor.services;

import lombok.Getter;

@Getter
public enum ResourceEnum {
    CAMUNDA_TASKS_UNCONFIGURED(
        "camunda/camunda-search-taskState-unconfigured.json"),
    CAMUNDA_TASKS_CFT_TASK_STATE_UNCONFIGURED(
        "camunda/camunda-search-cftTaskState-unconfigured.json"),
    DELETE_PROCESS_INSTANCES_JOB_SERVICE(
        "camunda/camunda-delete-process-instances-request-parameter.json"),
    AD_HOC_CREATE_TASKS(
        "adhoc/create-task/ad-hoc-create-tasks.json"),
    AD_HOC_CREATE_TASKS_CCD_ELASTIC_SEARCH_QUERY(
        "adhoc/create-task/ad-hoc-create-tasks-ccd-elastic-search-query.json"),
    AD_HOC_UPDATE_CASE_CCD_ELASTIC_SEARCH_QUERY(
        "adhoc/update-case/ad-hoc-update-case-ccd-elastic-search-query.json"),
    CAMUNDA_HISTORIC_TASKS_PENDING_TERMINATION(
        "camunda/camunda-historic-task-pending-termination-query.json"),
    CAMUNDA_TASKS_TERMINATION_FAILURES(
        "camunda/camunda-historic-task-unterminated-query.json"),
    CAMUNDA_CLEAN_UP_TASK_QUERY(
        "camunda/camunda-task-clean-up-query.json"),
    ACTIVE_PROCESS_DELETE_REQUEST(
        "camunda/camunda-delete-active-process-instances-request.json"),
    HISTORIC_PROCESS_DELETE_REQUEST(
        "camunda/camunda-delete-historic-process-instances-request.json");

    private final String resourcePath;

    ResourceEnum(String requestParameterBody) {
        this.resourcePath = requestParameterBody;
    }
}
