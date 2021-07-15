package uk.gov.hmcts.reform.wataskmonitor.services.jobs;

import lombok.Getter;

@Getter
public enum ResourceEnum {
    CONFIGURATION_JOB_SERVICE("camunda/camunda-query-parameters.json"),
    DELETE_PROCESS_INSTANCES_JOB_SERVICE(
        "camunda/camunda-delete-process-instances-request-parameter.json"),
    AD_HOC_CREATE_TASKS("ad-hoc-create-tasks.json");

    private final String resourcePath;

    ResourceEnum(String requestParameterBody) {
        this.resourcePath = requestParameterBody;
    }
}