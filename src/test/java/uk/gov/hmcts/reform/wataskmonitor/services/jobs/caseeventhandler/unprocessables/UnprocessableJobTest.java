package uk.gov.hmcts.reform.wataskmonitor.services.jobs.caseeventhandler.unprocessables;

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
class UnprocessableJobTest {
    public static final String SOME_SERVICE_TOKEN = "some service token";
    @Mock
    private UnprocessableJobService unprocessableJobService;
    @InjectMocks
    private UnprocessableJob unprocessableJob;

    @ParameterizedTest(name = "jobName: {0} expected: {1}")
    @CsvSource({
        "READY, false",
        "UNPROCESSABLE, true"
    })
    void canRun(JobName jobName, boolean expectedResult) {
        assertThat(unprocessableJob.canRun(jobName)).isEqualTo(expectedResult);
    }

    @Test
    void run() {
        List<String> messageIds = singletonList("someMessageId");
        when(unprocessableJobService.getUnprocessableMessages(SOME_SERVICE_TOKEN, JobName.UNPROCESSABLE))
            .thenReturn(messageIds);
        MessageJobReport messageJobReport = new MessageJobReport(
            1,
            messageIds
        );

        unprocessableJob.run(SOME_SERVICE_TOKEN);

        verify(unprocessableJobService).getUnprocessableMessages(SOME_SERVICE_TOKEN, JobName.UNPROCESSABLE);

    }
}
