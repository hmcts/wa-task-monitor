package uk.gov.hmcts.reform.wataskmonitor.services.jobs;

import lombok.Getter;

@Getter
public enum RequestsEnum {

    CAMUNDA_HISTORIC_TASKS_PENDING_TERMINATION("camunda/camunda-historic-task-pending-termination-query.json"),

    CAMUNDA_TASKS_UNCONFIGURED("camunda/camunda-query-parameters.json"),

    DELETE_PROCESS_INSTANCES_JOB_SERVICE(
        "camunda/camunda-delete-process-instances-request-parameter.json");

    private final String requestBodyLocation;

    RequestsEnum(String requestBodyLocation) {
        this.requestBodyLocation = requestBodyLocation;
    }
}
