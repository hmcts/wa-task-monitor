package uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation.helpers;

import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaVariable;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InitiationHelpers {

    private InitiationHelpers() {
        //Utility classes should not have a public or default constructor.
    }

    public static List<CamundaTask> getMockedTasks() {
        CamundaTask task1 = new CamundaTask(
            "some id",
            "task name 1",
            "2151a580-c3c3-11eb-8b76-d26a7287fec2"
        );
        CamundaTask task2 = new CamundaTask(
            "some other id",
            "task name 2",
            "2151a580-c3c3-11eb-8b76-d26a7287f000"
        );
        return List.of(task1, task2);

    }

    public static CamundaTask createMockedCamundaTask(ZonedDateTime createdDate, ZonedDateTime dueDate) {
        return new CamundaTask(
            "someCamundaTaskId",
            "someCamundaTaskName",
            "someAssignee",
            createdDate,
            dueDate,
            "someCamundaTaskDescription",
            "someCamundaTaskOwner",
            "someCamundaTaskFormKey",
            "someProcessInstanceId"
            );
    }

    public static Map<String, CamundaVariable> createMockCamundaVariables() {
        Map<String, CamundaVariable> variables = new HashMap<>();
        variables.put("caseId", new CamundaVariable("00000", "String"));
        variables.put("caseName", new CamundaVariable("someCaseName", "String"));
        variables.put("caseTypeId", new CamundaVariable("someCaseType", "String"));
        variables.put("taskState", new CamundaVariable("unconfigured", "String"));
        variables.put("location", new CamundaVariable("someStaffLocationId", "String"));
        variables.put("locationName", new CamundaVariable("someStaffLocationName", "String"));
        variables.put("securityClassification", new CamundaVariable("SC", "String"));
        variables.put("title", new CamundaVariable("someTitle", "String"));
        variables.put("executionType", new CamundaVariable("someExecutionType", "String"));
        variables.put("taskSystem", new CamundaVariable("someTaskSystem", "String"));
        variables.put("jurisdiction", new CamundaVariable("someJurisdiction", "String"));
        variables.put("region", new CamundaVariable("someRegion", "String"));
        variables.put("appealType", new CamundaVariable("someAppealType", "String"));
        variables.put("caseManagementCategory", new CamundaVariable("someCaseCategory", "String"));
        variables.put("autoAssigned", new CamundaVariable("false", "Boolean"));
        variables.put("assignee", new CamundaVariable("uid", "String"));
        variables.put("hasWarnings", new CamundaVariable("true", "Boolean"));
        variables.put("warningList", new CamundaVariable("SomeWarningListValue", "String"));
        variables.put("taskType", new CamundaVariable("someTaskType", "String"));
        return variables;
    }

}
