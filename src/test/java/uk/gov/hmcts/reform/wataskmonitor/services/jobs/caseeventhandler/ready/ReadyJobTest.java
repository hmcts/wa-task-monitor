package uk.gov.hmcts.reform.wataskmonitor.services.jobs.caseeventhandler.ready;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.caseeventhandler.MessageJobReport;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadyJobTest {
    public static final String SOME_SERVICE_TOKEN = "some service token";
    @Mock
    private ReadyJobService readyJobService;
    @InjectMocks
    private ReadyJob readyJob;

    @ParameterizedTest(name = "jobName: {1} expected: {0}")
    @CsvSource({
        "READY, true",
        "UNPROCESSABLE, false"
    })
    void canRun(JobName jobName, boolean expectedResult) {
        assertThat(readyJob.canRun(jobName)).isEqualTo(expectedResult);
    }

    @Test
    void run() {

        List<String> messageIds = singletonList("someMessageId");
        when(readyJobService.getReadyMessages(SOME_SERVICE_TOKEN, JobName.READY))
            .thenReturn(messageIds);
        MessageJobReport messageJobReport = new MessageJobReport(
            1,
            messageIds
        );

        readyJob.run(SOME_SERVICE_TOKEN);

        verify(readyJobService).getReadyMessages(SOME_SERVICE_TOKEN, JobName.READY);

    }
}
