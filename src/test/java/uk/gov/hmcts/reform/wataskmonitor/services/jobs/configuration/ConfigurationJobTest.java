package uk.gov.hmcts.reform.wataskmonitor.services.jobs.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurationJobTest {

    public static final String SERVICE_TOKEN = "some s2s token";
    @Mock
    private ConfigurationJobService configurationJobService;
    @InjectMocks
    private ConfigurationJob configurationJob;

    @ParameterizedTest(name = "jobName: {0} expected: {1}")
    @CsvSource({
        "TERMINATION, false",
        "INITIATION, false",
        "CONFIGURATION, true",
        "AD_HOC_DELETE_PROCESS_INSTANCES, false"
    })
    void canRun(JobName jobName, boolean expectedResult) {
        assertThat(configurationJob.canRun(jobName)).isEqualTo(expectedResult);
    }

    @Test
    void run() {
        CamundaTask camundaTask = mock(CamundaTask.class);
        List<CamundaTask> taskList = singletonList(camundaTask);
        when(configurationJobService.getUnConfiguredTasks(SERVICE_TOKEN))
            .thenReturn(taskList);

        configurationJob.run(SERVICE_TOKEN);

        verify(configurationJobService).getUnConfiguredTasks(SERVICE_TOKEN);
        verify(configurationJobService).configureTasks(taskList, SERVICE_TOKEN);
    }
}