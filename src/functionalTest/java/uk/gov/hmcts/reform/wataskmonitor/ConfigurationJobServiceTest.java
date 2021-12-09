package uk.gov.hmcts.reform.wataskmonitor;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks.CreateDelayedTaskJobService;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.adhoc.createtasks.CreateTaskJobService;
import uk.gov.hmcts.reform.wataskmonitor.services.jobs.configuration.ConfigurationJobService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LawOfDemeter"})
public class ConfigurationJobServiceTest extends SpringBootFunctionalBaseTest {

    private static String DELETE_REQUEST = "{\n"
                                           + "  \"deleteReason\": \"clean up running process instances\",\n"
                                           + "  \"processInstanceIds\": [\n"
                                           + "    \"PROCESS_INSTANCE_ID\"\n"
                                           + "  ],\n"
                                           + "  \"skipCustomListeners\": true,\n"
                                           + "  \"skipSubprocesses\": true,\n"
                                           + "  \"failIfNotExists\": false\n"
                                           + "}";

    private final long DELAY = 5 * 60 * 1000 + 10;

    private String processInstanceId;

    @Autowired
    private ConfigurationJobService configurationJobService;

    @Autowired
    private CreateDelayedTaskJobService createDelayedTaskJobService;

    @Autowired
    private CreateTaskJobService createTaskJobService;

    @Autowired
    private CamundaClient camundaClient;


    @After
    public void cleanUp() {

        camundaClient.deleteProcessInstance(serviceToken, getRequestParameter(processInstanceId));

    }

    @Test
    public void should_return_tasks_apart_from_delayed_tasks() {

        createTask();
        createDelayedTask();

        waitForConfiguration();

        List<CamundaTask> tasks = configurationJobService.getUnConfiguredTasks(serviceToken);
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals("Review Appeal Skeleton Argument", tasks.get(0).getName());
        assertNull(tasks.get(0).getAssignee());
        processInstanceId = tasks.get(0).getProcessInstanceId();
    }

    private void createTask() {
        createTaskJobService.createTasks(serviceToken);
    }

    private void createDelayedTask() {
        createDelayedTaskJobService.createTasks(serviceToken);
    }

    private String getRequestParameter(String processInstanceId) {
        return DELETE_REQUEST.replace("PROCESS_INSTANCE_ID", processInstanceId);
    }

    private void waitForConfiguration() {
        try {
            Thread.sleep(DELAY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
