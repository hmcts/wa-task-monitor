package uk.gov.hmcts.reform.wataskmonitor;

import org.junit.Test;
import uk.gov.hmcts.reform.wataskmonitor.entities.TestVariables;

import static org.junit.Assert.assertNotNull;

public class CreateTaskTest extends SpringBootFunctionalBaseTest {

    @Test
    public void should_create_a_task() {
        TestVariables taskVariables = common.setupTaskAndRetrieveIds();

        assertNotNull(taskVariables);
        assertNotNull(taskVariables.getTaskId());
        assertNotNull(taskVariables.getCaseId());

        //common.cleanUpTask(taskId);
    }


}
