package uk.gov.hmcts.reform.wataskmonitor.services;

import lombok.Getter;

@Getter
public enum ResourceEnum {
    CAMUNDA_TASKS_UNCONFIGURED(
        "camunda/camunda-search-taskState-unconfigured.json"),
    CAMUNDA_TASKS_CFT_TASK_STATE_UNCONFIGURED(
        "camunda/camunda-search-cftTaskState-unconfigured.json"),
    CAMUNDA_HISTORIC_TASKS_PENDING_TERMINATION(
        "camunda/camunda-historic-task-pending-termination-query.json"),
    CAMUNDA_TASKS_TERMINATION_FAILURES(
        "camunda/camunda-historic-task-unterminated-query.json"),
    CAMUNDA_HISTORY_CLEAN_UP_TASK_QUERY(
        "camunda/camunda-history-task-clean-up-query.json"),

    CAMUNDA_ACTIVE_CLEAN_UP_TASK_QUERY(
        "camunda/camunda-active-task-clean-up-query.json"),
    ACTIVE_PROCESS_DELETE_REQUEST(
        "camunda/camunda-delete-active-process-instances-request.json"),
    HISTORIC_PROCESS_DELETE_REQUEST(
        "camunda/camunda-delete-historic-process-instances-request.json");

    private final String resourcePath;

    ResourceEnum(String requestParameterBody) {
        this.resourcePath = requestParameterBody;
    }
}
