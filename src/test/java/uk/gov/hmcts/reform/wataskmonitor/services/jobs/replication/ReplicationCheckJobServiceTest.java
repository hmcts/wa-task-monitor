package uk.gov.hmcts.reform.wataskmonitor.services.jobs.replication;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskOperationClient;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class ReplicationCheckJobServiceTest extends UnitBaseTest {

    @Mock
    private TaskOperationClient taskOperationClient;

    @InjectMocks
    private ReplicationCheckJobService replicationCheckJobService;

    @Test
    void should_update_search_index() {

        String operationId = replicationCheckJobService.replicationCheck(SOME_SERVICE_TOKEN);

        assertFalse(operationId.isEmpty());

    }

}
