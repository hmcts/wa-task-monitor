package uk.gov.hmcts.reform.wataskmonitor.services.jobs.failure.reconfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskOperationClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReconfigurationFailuresJobServiceTest extends UnitBaseTest {

    @Mock
    private TaskOperationClient taskOperationClient;

    private ReconfigurationFailuresJobService reconfigurationFailuresJobService;

    @BeforeEach
    void setUp() {

        reconfigurationFailuresJobService = new ReconfigurationFailuresJobService(
                taskOperationClient, 120, 2);
    }

    @Test
    void should_execute_reconfigure_failures_task() {
        String operationId = reconfigurationFailuresJobService.reconfigureFailuresTask(SOME_SERVICE_TOKEN);
        assertNotNull(operationId);
    }

}
