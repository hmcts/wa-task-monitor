package uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.config.job.InitiationJobConfig;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaVariable;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.initiation.helpers.InitiationHelpers;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CamundaServiceTest {

    private static final String SOME_SERVICE_TOKEN = "some service token";

    @Mock
    private CamundaClient camundaClient;
    @Mock
    private InitiationJobConfig initiationJobConfig;
    @Captor
    private ArgumentCaptor<String> queryCaptor;

    private CamundaService camundaService;

    @BeforeEach
    void setUp() {
        camundaService = new CamundaService(camundaClient, initiationJobConfig);
        lenient().when(initiationJobConfig.getCamundaMaxResults()).thenReturn("100");
        lenient().when(initiationJobConfig.getCamundaTimeLimit()).thenReturn(120L);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void should_get_unconfigured_tasks_using_initiation_query(boolean timeLimitEnabled) throws JSONException {
        List<CamundaTask> tasks = InitiationHelpers.getMockedTasks();
        when(initiationJobConfig.isCamundaTimeLimitFlag()).thenReturn(timeLimitEnabled);
        when(camundaClient.getTasks(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            queryCaptor.capture()
        )).thenReturn(tasks);

        List<CamundaTask> result = camundaService.getInitiationCandidates(SOME_SERVICE_TOKEN);

        JSONObject query = new JSONObject(queryCaptor.getValue());
        assertThat(result).isEqualTo(tasks);
        assertThat(query.has("createdAfter")).isEqualTo(timeLimitEnabled);
        assertThat(query.has("createdBefore")).isFalse();
        assertThat(query.getString("taskDefinitionKey")).isEqualTo("processTask");
    }

    @Test
    void should_get_all_unconfigured_tasks_without_a_time_limit() throws JSONException {
        List<CamundaTask> tasks = InitiationHelpers.getMockedTasks();
        when(camundaClient.getTasks(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            queryCaptor.capture()
        )).thenReturn(tasks);

        List<CamundaTask> result = camundaService.getUnconfiguredTasks(SOME_SERVICE_TOKEN);

        JSONObject query = new JSONObject(queryCaptor.getValue());
        assertThat(result).isEqualTo(tasks);
        assertThat(query.has("createdBefore")).isFalse();
        assertThat(query.has("createdAfter")).isFalse();
        assertThat(query.getString("taskDefinitionKey")).isEqualTo("processTask");
    }

    @Test
    void should_get_stale_unconfigured_tasks_using_failure_query() throws JSONException {
        List<CamundaTask> tasks = InitiationHelpers.getMockedTasks();
        when(camundaClient.getTasks(
            eq(SOME_SERVICE_TOKEN),
            eq("0"),
            eq("100"),
            queryCaptor.capture()
        )).thenReturn(tasks);

        List<CamundaTask> result = camundaService.getStaleUnconfiguredTasks(SOME_SERVICE_TOKEN);

        JSONObject query = new JSONObject(queryCaptor.getValue());
        assertThat(result).isEqualTo(tasks);
        assertThat(query.has("createdBefore")).isTrue();
        assertThat(query.has("createdAfter")).isFalse();
    }

    @Test
    void should_get_task_variables() {
        Map<String, CamundaVariable> variables = InitiationHelpers.createMockCamundaVariables();
        when(camundaClient.getVariables(SOME_SERVICE_TOKEN, "task-id")).thenReturn(variables);

        Map<String, CamundaVariable> result = camundaService.getTaskVariables(
            SOME_SERVICE_TOKEN,
            "task-id"
        );

        assertThat(result).isEqualTo(variables);
        verify(camundaClient).getVariables(SOME_SERVICE_TOKEN, "task-id");
    }
}
