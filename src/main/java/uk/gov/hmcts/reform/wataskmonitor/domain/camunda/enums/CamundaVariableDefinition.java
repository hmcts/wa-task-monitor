package uk.gov.hmcts.reform.wataskmonitor.domain.camunda.enums;

public enum CamundaVariableDefinition {
    APPEAL_TYPE("appealType"),
    AUTO_ASSIGNED("autoAssigned"),
    ASSIGNEE("assignee"),
    CASE_ID("caseId"),
    CASE_NAME("caseName"),
    CASE_TYPE_ID("caseTypeId"),
    EXECUTION_TYPE("executionType"),
    FORM_KEY("formKey"),
    JURISDICTION("jurisdiction"),
    LOCATION("location"),
    LOCATION_NAME("locationName"),
    REGION("region"),
    SECURITY_CLASSIFICATION("securityClassification"),
    TASK_ID("taskId"),
    TASK_NAME("name"),
    TASK_STATE("taskState"),
    TASK_SYSTEM("taskSystem"),
    TASK_TYPE("taskType"),
    TITLE("title"),
    HAS_WARNINGS("hasWarnings"),
    WARNING_LIST("warningList"),
    CFT_TASK_STATE("cftTaskState"),
    CASE_MANAGEMENT_CATEGORY("caseManagementCategory");


    private final String value;

    CamundaVariableDefinition(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
