package uk.gov.hmcts.reform.wataskmonitor.services.jobs.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfigurationJobTest extends UnitBaseTest {

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
        CamundaTask camundaTask = new CamundaTask(
            "some taskId",
            "some name",
            "someProcessInstanceId"
        );
        List<CamundaTask> taskList = singletonList(camundaTask);
        when(configurationJobService.getUnConfiguredTasks(SOME_SERVICE_TOKEN))
            .thenReturn(taskList);
        GenericJobReport jobReport = new GenericJobReport(
            1,
            singletonList(GenericJobOutcome.builder()
                              .taskId("some taskId")
                              .processInstanceId("some processInstanceId")
                              .successful(true)
                              .jobType("Task Configuration")
                              .build())
        );
        when(configurationJobService.configureTasks(taskList, SOME_SERVICE_TOKEN))
            .thenReturn(jobReport);

        configurationJob.run(SOME_SERVICE_TOKEN);

        verify(configurationJobService).getUnConfiguredTasks(SOME_SERVICE_TOKEN);
        verify(configurationJobService).configureTasks(taskList, SOME_SERVICE_TOKEN);
    }
}
