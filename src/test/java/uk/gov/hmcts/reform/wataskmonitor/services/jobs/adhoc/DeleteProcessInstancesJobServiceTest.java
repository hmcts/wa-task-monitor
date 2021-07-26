package uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.wataskmonitor.UnitBaseTest;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.deleteprocessinstances.DeleteProcessInstancesJobService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeleteProcessInstancesJobServiceTest extends UnitBaseTest {

    @Mock
    private CamundaClient camundaClient;
    @InjectMocks
    private DeleteProcessInstancesJobService jobService;

    @Test
    void deleteProcessInstances() {
        when(camundaClient.deleteProcessInstance(eq("some s2s token"), anyString()))
            .thenReturn("some response");

        jobService.deleteProcessInstances("some s2s token");

        verify(camundaClient).deleteProcessInstance(eq("some s2s token"), anyString());
    }
}
