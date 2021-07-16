package uk.gov.hmcts.reform.wataskmonitor.services.jobs;

import lombok.Getter;

@Getter
public enum RequestsEnum {
    CONFIGURATION_JOB_SERVICE("camunda/camunda-query-parameters.json"),

    DELETE_PROCESS_INSTANCES_JOB_SERVICE(
        "camunda/camunda-delete-process-instances-request-parameter.json");

    private final String requestParameterBody;

    RequestsEnum(String requestParameterBody) {
        this.requestParameterBody = requestParameterBody;
    }
}
