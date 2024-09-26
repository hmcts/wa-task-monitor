package uk.gov.hmcts.reform.wataskmonitor.services.jobs.cleanup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.config.job.CleanUpJobConfig;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobOutcome;
import uk.gov.hmcts.reform.wataskmonitor.domain.jobs.GenericJobReport;
import uk.gov.hmcts.reform.wataskmonitor.services.ResourceEnum;
import uk.gov.hmcts.reform.wataskmonitor.utils.ResourceUtility;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.wataskmonitor.domain.taskmonitor.JobName.MAINTENANCE_CAMUNDA_TASK_CLEAN_UP;
import static uk.gov.hmcts.reform.wataskmonitor.services.ResourceEnum.ACTIVE_PROCESS_DELETE_REQUEST;
import static uk.gov.hmcts.reform.wataskmonitor.services.ResourceEnum.HISTORIC_PROCESS_DELETE_REQUEST;

@Component
@Slf4j
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class MaintenanceCamundaTaskCleanUpJobService {

    public static final String CAMUNDA_DATE_TIME_REQUEST_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CAMUNDA_DATE_TIME_REQUEST_PATTERN);

    private final CamundaClient camundaClient;
    private final CleanUpJobConfig cleanUpJobConfig;

    @Autowired
    public MaintenanceCamundaTaskCleanUpJobService(CamundaClient camundaClient,
                                                   CleanUpJobConfig cleanUpJobConfig) {
        this.camundaClient = camundaClient;
        this.cleanUpJobConfig = cleanUpJobConfig;
    }

    public boolean isAllowedEnvironment() {
        log.info("{} cleanUpJobConfig: {}", MAINTENANCE_CAMUNDA_TASK_CLEAN_UP.name(), cleanUpJobConfig);

        if (!cleanUpJobConfig.getAllowedEnvironment()
            .contains(cleanUpJobConfig.getEnvironment().toLowerCase(Locale.ROOT))) {
            log.info("{} is not enabled for this environment: {}",
                MAINTENANCE_CAMUNDA_TASK_CLEAN_UP.name(), cleanUpJobConfig.getEnvironment());
            return false;
        }

        if ("prod".equalsIgnoreCase(cleanUpJobConfig.getEnvironment())) {
            log.info("{} job can not be working on PROD environment", MAINTENANCE_CAMUNDA_TASK_CLEAN_UP.name());
            return false;
        }

        log.info("{} is enabled for this environment: {}", MAINTENANCE_CAMUNDA_TASK_CLEAN_UP.name(),
            cleanUpJobConfig.getEnvironment());
        return true;
    }

    public List<HistoricCamundaTask> retrieveHistoricProcesses() {
        if (!isAllowedEnvironment()) {
            return emptyList();
        }

        List<HistoricCamundaTask> historyTasks = camundaClient.getHistoryProcesses(
            "0",
            cleanUpJobConfig.getCleanUpCamundaMaxResults(),
            buildSearchQuery(ResourceEnum.CAMUNDA_HISTORY_CLEAN_UP_TASK_QUERY)
        );
        log.info("{} task(s) retrieved successfully from history", historyTasks.size());
        return historyTasks;
    }

    public List<HistoricCamundaTask> retrieveActiveProcesses() {
        if (!isAllowedEnvironment()) {
            return emptyList();
        }

        List<HistoricCamundaTask> activeProcesses = camundaClient.getHistoryProcesses(
            "0",
            cleanUpJobConfig.getCleanUpCamundaMaxResults(),
            buildSearchQuery(ResourceEnum.CAMUNDA_ACTIVE_CLEAN_UP_TASK_QUERY)
        );
        log.info("{} active processes retrieved successfully", activeProcesses.size());
        return activeProcesses;
    }

    public GenericJobReport deleteHistoricProcesses(List<HistoricCamundaTask> historicCamundaTasks,
                                                    String serviceToken) {
        if (historicCamundaTasks == null || historicCamundaTasks.isEmpty()) {
            log.info("There was no task(s) to delete.");
            return new GenericJobReport(0, emptyList());
        } else {
            List<GenericJobOutcome> outcomesList;

            outcomesList = deleteHistoricProcessesFromCamunda(historicCamundaTasks, serviceToken);

            return new GenericJobReport(historicCamundaTasks.size(), outcomesList);
        }
    }

    public GenericJobReport deleteActiveProcesses(List<HistoricCamundaTask> historicCamundaTasks, String serviceToken) {

        if (!isCleanActiveTaskAllowed()) {
            log.info("Clean active tasks not allowed");
            return new GenericJobReport(0, emptyList());
        }

        if (historicCamundaTasks == null || historicCamundaTasks.isEmpty()) {
            log.info("There was no active task(s) to delete.");
            return new GenericJobReport(0, emptyList());
        } else {
            List<GenericJobOutcome> outcomesList;

            outcomesList = deleteActiveProcessesFromCamunda(historicCamundaTasks, serviceToken);

            return new GenericJobReport(historicCamundaTasks.size(), outcomesList);
        }
    }

    private List<GenericJobOutcome> deleteHistoricProcessesFromCamunda(List<HistoricCamundaTask> historicCamundaTasks,
                                                                       String serviceToken) {
        List<GenericJobOutcome> outcomeList = new ArrayList<>();
        boolean isSuccess = false;

        List<String> historicProcessInstanceIds = historicCamundaTasks.stream()
            .map(HistoricCamundaTask::getId).toList();


        String body = prepareRequestBody(HISTORIC_PROCESS_DELETE_REQUEST, historicProcessInstanceIds);

        try {
            camundaClient.deleteHistoryProcesses(
                serviceToken,
                body
            );
            isSuccess = true;
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
            .map(HistoricCamundaTask::getId).toList();

        String body = prepareRequestBody(ACTIVE_PROCESS_DELETE_REQUEST, processInstanceIds);

        try {
            camundaClient.deleteActiveProcesses(
                serviceToken,
                body
            );
            isSuccess = true;
        } catch (Exception e) {
            log.error("An error occurred when deleting active tasks : {}", e.getMessage());
        }

        outcomeList.add(createJobOutCome(processInstanceIds, isSuccess));

        return outcomeList;
    }

    private String buildSearchQuery(ResourceEnum resource) {
        return ResourceUtility.getResource(resource)
            .replace("STARTED_BEFORE_PLACE_HOLDER", getStartedBefore());
    }

    private String getStartedBefore() {
        ZonedDateTime startedBeforeDateTime = ZonedDateTime.now()
            .minusDays(cleanUpJobConfig.getCleanUpStartedDaysBefore());

        return startedBeforeDateTime.format(formatter);
    }

    private String prepareRequestBody(ResourceEnum resourceEnum, List<String> ids) {
        return ResourceUtility.getResource(resourceEnum)
            .replace("PROCESS_INSTANCE_ID_PLACEHOLDER",
                String.join("\",\"", ids));
    }

    private GenericJobOutcome createJobOutCome(List<String> historicProcessInstanceIds, boolean isSuccess) {
        return GenericJobOutcome.builder()
            .taskId(null)
            .processInstanceId(String.join(", ", historicProcessInstanceIds))
            .successful(isSuccess)
            .jobType(MAINTENANCE_CAMUNDA_TASK_CLEAN_UP.name())
            .build();
    }

    private boolean isCleanActiveTaskAllowed() {

        if (!cleanUpJobConfig.getAllowedEnvironment()
            .contains(cleanUpJobConfig.getEnvironment().toLowerCase(Locale.ROOT))) {
            log.info("{} is not enabled for this environment: {}",
                MAINTENANCE_CAMUNDA_TASK_CLEAN_UP.name(), cleanUpJobConfig.getEnvironment());
            return false;
        }

        if ("prod".equalsIgnoreCase(cleanUpJobConfig.getEnvironment())) {
            log.info("{} job can not be working on PROD environment", MAINTENANCE_CAMUNDA_TASK_CLEAN_UP.name());
            return false;
        }

        log.info("{} clean active task is enabled for this environment: {}",
            MAINTENANCE_CAMUNDA_TASK_CLEAN_UP.name(), cleanUpJobConfig.getEnvironment());
        return true;
    }

}
