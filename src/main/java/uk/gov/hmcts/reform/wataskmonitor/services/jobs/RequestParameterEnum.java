package uk.gov.hmcts.reform.wataskmonitor.services.jobs;

import lombok.Getter;

@Getter
public enum RequestParameterEnum {
    CONFIGURATION_JOB_SERVICE("camunda/camunda-query-parameters.json"),

    DELETE_PROCESS_INSTANCES_JOB_SERVICE(
        "camunda/camunda-delete-process-instances-request-parameter.json");

    private final String requestParameterBody;

    RequestParameterEnum(String requestParameterBody) {
        this.requestParameterBody = requestParameterBody;
    }
}