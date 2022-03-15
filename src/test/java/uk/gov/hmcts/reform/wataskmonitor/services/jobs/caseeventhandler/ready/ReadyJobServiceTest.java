package uk.gov.hmcts.reform.wataskmonitor.services.jobs.caseeventhandler.ready;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.wataskmonitor.clients.CaseEventHandlerClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest.SOME_SERVICE_TOKEN;

@ExtendWith(MockitoExtension.class)
class ReadyJobServiceTest {

    private ReadyJobService readyJobService;
    @Mock
    private CaseEventHandlerClient caseEventHandlerClient;

    @BeforeEach
    void setUp() {
        readyJobService = new ReadyJobService(caseEventHandlerClient);
    }

    @Test
    void should_return_message_ids() {
        List<String> messageIds = singletonList("someMessageId");
        when(caseEventHandlerClient.findProblematicMessages(SOME_SERVICE_TOKEN, JobName.READY.name()))
            .thenReturn(messageIds);

        List<String> actualMessageIds = readyJobService.getReadyMessages(SOME_SERVICE_TOKEN, JobName.READY);

        assertThat(actualMessageIds).isEqualTo(messageIds);
    }

    @Test
    void should_return_empty_list() {

        List<String> actual = readyJobService.getReadyMessages(SOME_SERVICE_TOKEN, JobName.READY);

        assertEquals(emptyList(), actual);
    }
}
