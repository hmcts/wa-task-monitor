package uk.gov.hmcts.reform.wataskmonitor.services.jobs.cleanup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.config.job.CleanUpJobConfig;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaHistoryRemoveRequest;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaRemoveRequest;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTaskCount;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
import uk.gov.hmcts.reform.wataskmonitor.utils.LoggingUtility;
import uk.gov.hmcts.reform.wataskmonitor.utils.ResourceUtility;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.TASK_CLEAN_UP;
import static uk.gov.hmcts.reform.wataskmonitor.services.ResourceEnum.CAMUNDA_CLEAN_UP_TASK_QUERY;

@Component
@Slf4j
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class CleanUpJobService {

    public static final String CAMUNDA_DATE_TIME_REQUEST_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CAMUNDA_DATE_TIME_REQUEST_PATTERN);

    private final CamundaClient camundaClient;
    private final CleanUpJobConfig cleanUpJobConfig;
    private final ObjectMapper objectMapper;

    @Autowired
    public CleanUpJobService(CamundaClient camundaClient,
                             CleanUpJobConfig cleanUpJobConfig,
                             ObjectMapper objectMapper) {
        this.camundaClient = camundaClient;
        this.cleanUpJobConfig = cleanUpJobConfig;
        this.objectMapper = objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
    }

    public boolean isAllowedEnvironment() {
        log.info("{} cleanUpJobConfig: {}", TASK_CLEAN_UP.name(), cleanUpJobConfig.getEnvironment());

        if (cleanUpJobConfig.getEnvironment().equalsIgnoreCase("prod")) {
            log.info("{} is not enabled for this environment: {}",
                TASK_CLEAN_UP.name(), cleanUpJobConfig.getEnvironment());
            return false;
        }

        log.info("{} is enabled for this environment: {}", TASK_CLEAN_UP.name(), cleanUpJobConfig.getEnvironment());
        return true;
    }

    public List<HistoricCamundaTask> retrieveProcesses() {
        List<HistoricCamundaTask> historyTasks = camundaClient.getHistoryProcesses(
            "0",
            cleanUpJobConfig.getCamundaMaxResults(),
            buildSearchQuery()
        );
        log.info("{} task(s) retrieved successfully from history", historyTasks.size());
        return historyTasks;
    }

    public GenericJobReport deleteHistoricProcesses(List<HistoricCamundaTask> historicCamundaTasks,
                                                    String serviceToken) {
        if (historicCamundaTasks == null || historicCamundaTasks.isEmpty()) {
            log.info("There was no task(s) to delete.");
            return new GenericJobReport(0, emptyList());
        } else {
            List<GenericJobOutcome> outcomesList;
            log.info("There are {} task(s) in history before deletion", getHistoryTaskCount().getCount());

            outcomesList = deleteHistoricProcessesFromCamunda(historicCamundaTasks, serviceToken);

            AtomicReference<Long> count = new AtomicReference<>(0L);
            await()
                .pollDelay(5, SECONDS)
                .atMost(10, SECONDS)
                .until(() -> {
                    count.set(getHistoryTaskCount().getCount());
                    return count.get() != null;
                });
            log.info("There are {} task(s) in history after deletion", count.get());

            return new GenericJobReport(historicCamundaTasks.size(), outcomesList);
        }
    }

    public GenericJobReport deleteActiveProcesses(List<HistoricCamundaTask> historicCamundaTasks, String serviceToken) {

        if (!isCleanActiveTaskAllowed()) {
            return new GenericJobReport(0, emptyList());
        }

        if (historicCamundaTasks == null || historicCamundaTasks.isEmpty()) {
            log.info("There was no active task(s) to delete.");
            return new GenericJobReport(0, emptyList());
        } else {
            List<GenericJobOutcome> outcomesList;
            log.info("There are {} active task(s) before deletion", getActiveTaskCount().getCount());

            outcomesList = deleteActiveProcessesFromCamunda(historicCamundaTasks, serviceToken);

            AtomicReference<Long> count = new AtomicReference<>(0L);
            await()
                .pollDelay(5, SECONDS)
                .atMost(10, SECONDS)
                .until(() -> {
                    count.set(getActiveTaskCount().getCount());
                    return count.get() != null;
                });
            log.info("There are {} active task(s) after deletion", count.get());

            return new GenericJobReport(historicCamundaTasks.size(), outcomesList);
        }
    }

    private List<GenericJobOutcome> deleteHistoricProcessesFromCamunda(List<HistoricCamundaTask> historicCamundaTasks,
                                                                       String serviceToken) {
        List<GenericJobOutcome> outcomeList = new ArrayList<>();
        boolean isSuccess = false;

        List<String> historicProcessInstanceIds = historicCamundaTasks.stream()
            .map(HistoricCamundaTask::getId).collect(Collectors.toList());

        CamundaHistoryRemoveRequest camundaHistoryRemoveRequest = new CamundaHistoryRemoveRequest(
            "Timeout", historicProcessInstanceIds
        );

        try {
            camundaClient.deleteHistoryProcesses(
                serviceToken,
                objectMapper.writeValueAsString(camundaHistoryRemoveRequest)
            );
            isSuccess = true;
            log.info("History tasks successfully deleted");
        } catch (Exception e) {
            log.error("An error occurred when deleting history tasks : {}", e.getMessage());
        }

        outcomeList.add(createJobOutCome(historicProcessInstanceIds, isSuccess));

        return outcomeList;
    }

    private List<GenericJobOutcome> deleteActiveProcessesFromCamunda(List<HistoricCamundaTask> activeCamundaTasks,
                                                                     String serviceToken) {
        List<GenericJobOutcome> outcomeList = new ArrayList<>();
        boolean isSuccess = false;

        List<String> processInstanceIds = activeCamundaTasks.stream()
            .map(HistoricCamundaTask::getId).collect(Collectors.toList());

        CamundaRemoveRequest camundaHistoryRemoveRequest = new CamundaRemoveRequest(
            "Completed",
            processInstanceIds
        );

        try {
            camundaClient.deleteActiveProcesses(
                serviceToken,
                objectMapper.writeValueAsString(camundaHistoryRemoveRequest)
            );
            isSuccess = true;
            log.info("Active tasks successfully deleted");
        } catch (Exception e) {
            log.error("An error occurred when deleting active tasks : {}", e.getMessage());
        }

        outcomeList.add(createJobOutCome(processInstanceIds, isSuccess));

        return outcomeList;
    }

    private String buildSearchQuery() {
        String query = ResourceUtility.getResource(CAMUNDA_CLEAN_UP_TASK_QUERY);

        query = query
            .replace("STARTED_BEFORE_PLACE_HOLDER", getStartedBefore());

        log.info("{} job build query : {}", TASK_CLEAN_UP, LoggingUtility.logPrettyPrint(query));
        return query;
    }

    private CamundaTaskCount getHistoryTaskCount() {

        return camundaClient.getHistoryProcessCount(
            getStartedBefore()
        );

    }

    private CamundaTaskCount getActiveTaskCount() {

        return camundaClient.getActiveProcessCount(
            getStartedBefore()
        );

    }

    private String getStartedBefore() {
        ZonedDateTime startedBeforeDateTime = ZonedDateTime.now()
            .minusDays(cleanUpJobConfig.getStartedBeforeDays());

        return startedBeforeDateTime.format(formatter);
    }

    private GenericJobOutcome createJobOutCome(List<String> historicProcessInstanceIds, boolean isSuccess) {
        return GenericJobOutcome.builder()
            .taskId(null)
            .processInstanceId(String.join(", ", historicProcessInstanceIds))
            .successful(isSuccess)
            .jobType(TASK_CLEAN_UP.name())
            .build();
    }

    private boolean isCleanActiveTaskAllowed() {

        if (!cleanUpJobConfig.getEnvironment().equalsIgnoreCase("aat")) {
            log.info("{} clean active task is not enabled for this environment: {}",
                TASK_CLEAN_UP.name(), cleanUpJobConfig.getEnvironment());
            return false;
        }

        log.info("{} clean active task is enabled for this environment: {}",
            TASK_CLEAN_UP.name(), cleanUpJobConfig.getEnvironment());
        return true;
    }

}
