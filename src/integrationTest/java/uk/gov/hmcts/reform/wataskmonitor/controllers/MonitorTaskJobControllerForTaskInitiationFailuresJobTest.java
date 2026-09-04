package uk.gov.hmcts.reform.wataskmonitor.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.TestUtility;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskManagementClient;
import uk.gov.hmcts.reform.wataskmonitor.config.LaunchDarklyFeatureFlagProvider;
import uk.gov.hmcts.reform.wataskmonitor.config.features.FeatureFlag;
import uk.gov.hmcts.reform.wataskmonitor.config.job.InitiationJobConfig;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaVariable;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.InitiateTaskRequest;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.JobDetails;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.request.MonitorTaskJobRequest;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.wataskmonitor.controllers.MonitorTaskJobControllerUtility.expectedResponse;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmanagement.request.enums.InitiateTaskOperation.INITIATION;

class MonitorTaskJobControllerForTaskInitiationFailuresJobTest extends SpringBootIntegrationBaseTest {

    private static final String SERVICE_TOKEN = "some service token";
    private static final String CAMUNDA_TASK_ID = "some camunda task id";

    @MockitoBean
    private CamundaClient camundaClient;
    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;
    @MockitoBean
    private TaskManagementClient taskManagementClient;
    @MockitoBean
    private InitiationJobConfig initiationJobConfig;
    @MockitoBean
    private LaunchDarklyFeatureFlagProvider launchDarklyFeatureFlagProvider;

    @BeforeEach
    void setUp() {
        mockExternalDependencies();
    }

    @Test
    void shouldSucceedAndInitiateFailedTasks() throws Exception {
        MonitorTaskJobRequest monitorTaskJobReq = new MonitorTaskJobRequest(
            new JobDetails(JobName.TASK_INITIATION_FAILURES)
        );

        mockMvc.perform(post("/monitor/tasks/jobs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(TestUtility.asJsonString(monitorTaskJobReq)))
            .andExpect(status().isOk())
            .andExpect(content().string(equalTo(expectedResponse.apply(JobName.TASK_INITIATION_FAILURES.name()))));

        verify(authTokenGenerator).generate();
        verify(camundaClient).getTasks(
            eq(SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            any()
        );
        verify(camundaClient).getVariables(SERVICE_TOKEN, CAMUNDA_TASK_ID);

        ArgumentCaptor<InitiateTaskRequest> initiateTaskRequestCaptor =
            ArgumentCaptor.forClass(InitiateTaskRequest.class);
        verify(taskManagementClient).initiateTask(
            eq(SERVICE_TOKEN),
            eq(CAMUNDA_TASK_ID),
            initiateTaskRequestCaptor.capture()
        );

        InitiateTaskRequest initiateTaskRequest = initiateTaskRequestCaptor.getValue();
        assertThat(initiateTaskRequest.getOperation()).isEqualTo(INITIATION);
        assertThat(initiateTaskRequest.getTaskAttributes())
            .containsEntry("caseId", "00000")
            .containsEntry("taskType", "someTaskType");
    }

    private void mockExternalDependencies() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(launchDarklyFeatureFlagProvider.getBooleanValue(FeatureFlag.WA_INITIATE_TASKS_ON_CREATE))
            .thenReturn(true);
        when(initiationJobConfig.getCamundaMaxResults()).thenReturn("100");
        when(initiationJobConfig.isCamundaTimeLimitFlag()).thenReturn(true);
        when(initiationJobConfig.getCamundaTimeLimit()).thenReturn(120L);

        when(camundaClient.getTasks(
            eq(SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            any()
        )).thenReturn(List.of(createMockedCamundaTask()));

        when(camundaClient.getVariables(SERVICE_TOKEN, CAMUNDA_TASK_ID))
            .thenReturn(createMockCamundaVariables());

        doNothing().when(taskManagementClient).initiateTask(any(), any(), any());
    }

    private CamundaTask createMockedCamundaTask() {
        ZonedDateTime createdDate = ZonedDateTime.now();
        return new CamundaTask(
            CAMUNDA_TASK_ID,
            "someCamundaTaskName",
            "someProcessInstanceId",
            "someAssignee",
            createdDate,
            createdDate.plusDays(1),
            "someCamundaTaskDescription",
            "someCamundaTaskOwner",
            "someCamundaTaskFormKey"
        );
    }

    private Map<String, CamundaVariable> createMockCamundaVariables() {
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
