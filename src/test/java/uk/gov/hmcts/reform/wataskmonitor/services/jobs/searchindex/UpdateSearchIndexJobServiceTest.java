package uk.gov.hmcts.reform.wataskmonitor.services.jobs.searchindex;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.clients.TaskOperationClient;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class UpdateSearchIndexJobServiceTest extends UnitBaseTest {

    @Mock
    private TaskOperationClient taskOperationClient;

    @InjectMocks
    private UpdateSearchIndexJobService updateSearchIndexJobService;

    @Test
    void should_update_search_index() {

        String operationId = updateSearchIndexJobService.updateSearchIndex(SOME_SERVICE_TOKEN);

        assertFalse(operationId.isEmpty());

    }

}
