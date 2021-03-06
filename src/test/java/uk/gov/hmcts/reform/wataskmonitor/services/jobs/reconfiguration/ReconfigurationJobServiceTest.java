package uk.gov.hmcts.reform.wataskmonitor.services.jobs.reconfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskReconfigurationClient;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReconfigurationJobServiceTest extends UnitBaseTest {

    @Mock
    private TaskReconfigurationClient taskReconfigurationClient;

    private ReconfigurationJobService reconfigurationJobService;

    @BeforeEach
    void setUp() {

        reconfigurationJobService = new ReconfigurationJobService(
                                            taskReconfigurationClient,
                                            2);
    }

    @Test
    void should_execute_reconfigure_task_with_reconfigure_time() {

        String operationId = reconfigurationJobService.reconfigureTask(SOME_SERVICE_TOKEN);

        assertEquals(false, operationId.isEmpty());

    }

}
