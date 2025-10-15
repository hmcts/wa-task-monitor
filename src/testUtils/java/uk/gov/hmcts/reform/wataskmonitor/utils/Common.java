package uk.gov.hmcts.reform.wataskmonitor.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.restassured.http.Headers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.config.GivensBuilder;
import uk.gov.hmcts.reform.wataskmonitor.config.RestApiActions;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaVariable;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoricCamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.HistoryVariableInstance;
import uk.gov.hmcts.reform.wataskmonitor.domain.idam.UserInfo;
import uk.gov.hmcts.reform.wataskmonitor.entities.RoleAssignment;
import uk.gov.hmcts.reform.wataskmonitor.entities.RoleAssignmentResource;
import uk.gov.hmcts.reform.wataskmonitor.entities.TestVariables;
import uk.gov.hmcts.reform.wataskmonitor.entities.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskmonitor.services.AuthorizationProvider;
import uk.gov.hmcts.reform.wataskmonitor.services.IdamService;
import uk.gov.hmcts.reform.wataskmonitor.services.RoleAssignmentServiceApi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.hmcts.reform.wataskmonitor.config.SecurityConfiguration.AUTHORIZATION;
import static uk.gov.hmcts.reform.wataskmonitor.config.SecurityConfiguration.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.wataskmonitor.entities.camunda.CamundaValue.stringValue;
import static uk.gov.hmcts.reform.wataskmonitor.entities.enums.RoleType.CASE;
import static uk.gov.hmcts.reform.wataskmonitor.entities.enums.RoleType.ORGANISATION;
import static uk.gov.hmcts.reform.wataskmonitor.services.ResourceEnum.CAMUNDA_HISTORIC_TASKS_PENDING_TERMINATION;

@Slf4j
public class Common {

    public static final String R2_ROLE_ASSIGNMENT_REQUEST
        = "requests/roleAssignment/r2/set-organisational-role-assignment-request.json";
    public static final DateTimeFormatter CAMUNDA_DATA_TIME_FORMATTER = ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    public static final DateTimeFormatter ROLE_ASSIGNMENT_DATA_TIME_FORMATTER = ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
    private static String DELETE_REQUEST = "{\n"
        + "    \"deleteReason\": \"clean up running process instances\",\n"
        + "    \"processInstanceIds\": [\n"
        + "    \"{PROCESS_ID}\"\n"
        + "    ],\n"
        + "    \"skipCustomListeners\": true,\n"
        + "    \"skipSubprocesses\": true,\n"
        + "    \"failIfNotExists\": false\n"
        + "    }";

    private final GivensBuilder given;
    private final RestApiActions camundaApiActions;
    private final AuthorizationProvider authorizationProvider;
    private final RestApiActions taskManagementApiActions;

    private final IdamService idamService;
    private final RoleAssignmentServiceApi roleAssignmentServiceApi;
    private final CamundaClient camundaClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Common(GivensBuilder given,
                  RestApiActions camundaApiActions,
                  AuthorizationProvider authorizationProvider,
                  IdamService idamService,
                  RoleAssignmentServiceApi roleAssignmentServiceApi,
                  CamundaClient camundaClient,
                  RestApiActions taskManagementApiActions) {
        this.given = given;
        this.camundaApiActions = camundaApiActions;
        this.authorizationProvider = authorizationProvider;
        this.idamService = idamService;
        this.roleAssignmentServiceApi = roleAssignmentServiceApi;
        this.camundaClient = camundaClient;
        this.taskManagementApiActions = taskManagementApiActions;
    }


    public TestVariables setupTaskAndRetrieveIds() {

        String caseId = given.createCcdCase();

        List<CamundaTask> response = given
            .createTaskWithCaseId(caseId)
            .and()
            .retrieveTaskWithProcessVariableFilter("caseId", caseId);

        if (response.size() > 1) {
            fail("Search was not an exact match and returned more than one task used: " + caseId);
        }

        return new TestVariables(caseId, response.get(0).getId(), response.get(0).getProcessInstanceId());
    }

    public TestVariables setupDelayedTaskAndRetrieveIds() {

        String caseId = given.createCcdCase();

        List<CamundaTask> camundaTasks = given
            .createDelayedTaskWithCaseId(caseId)
            .and()
            .retrieveDelayedTaskWithProcessVariableFilter("caseId", caseId);

        if (camundaTasks.size() > 1) {
            fail("Search was not an exact match and returned more than one task used: " + caseId);
        }

        return new TestVariables(caseId, camundaTasks.get(0).getId(), StringUtils.EMPTY);
    }

    public void setupOrganisationalRoleAssignment(Headers headers) {

        UserInfo userInfo = authorizationProvider.getUserInfo(headers.getValue(AUTHORIZATION));


        //Clean/Reset user
        clearAllRoleAssignmentsForUser(userInfo.getUid(), headers);

        log.info("Creating Organizational Role");
        createCaseAllocator(userInfo, headers);
        createSupervisor(userInfo, headers);

        Map<String, String> attributes = Map.of(
            "primaryLocation", "765324",
            "region", "1",
            //This value must match the camunda task location variable for the permission check to pass
            "baseLocation", "765324",
            "jurisdiction", "WA"
        );
        createTribunalCaseWorker(userInfo, headers, attributes);
    }

    public void setupOrganisationalRoleAssignmentWithCustomAttributes(Headers headers, Map<String, String> attributes) {

        UserInfo userInfo = idamService.getUserInfo(headers.getValue(AUTHORIZATION));

        //Clean/Reset user
        clearAllRoleAssignmentsForUser(userInfo.getUid(), headers);

        createCaseAllocator(userInfo, headers);
        createSupervisor(userInfo, headers);
        createTribunalCaseWorker(userInfo, headers, attributes);
    }

    public void clearAllRoleAssignments(Headers headers) {
        UserInfo userInfo = idamService.getUserInfo(headers.getValue(AUTHORIZATION));
        clearAllRoleAssignmentsForUser(userInfo.getUid(), headers);
    }

    public void cleanUpTask(Headers authenticationHeaders, List<String> caseIds) {

        Set<String> processIds = new HashSet<>();

        caseIds
            .forEach(caseId -> processIds.addAll(getProcesses(authenticationHeaders, caseId)));

        processIds
            .forEach(processId -> deleteProcessInstance(authenticationHeaders, processId));

    }

    public Map<String, CamundaVariable> getTaskVariables(Headers headers, String taskId) {
        String serviceToken = headers.getValue(SERVICE_AUTHORIZATION);

        return camundaClient.getVariables(
            serviceToken,
            taskId
        );
    }

    public List<HistoricCamundaTask> getTasksFromHistory(Headers headers) {
        String serviceToken = headers.getValue(SERVICE_AUTHORIZATION);
        String query = ResourceUtility.getResource(CAMUNDA_HISTORIC_TASKS_PENDING_TERMINATION);
        query = query.replace("\"finishedAfter\": \"*\",", "");

        return camundaClient.getTasksFromHistory(
            serviceToken,
            "0",
            "1",
            query
        );
    }

    public List<HistoryVariableInstance> getTaskHistoryVariable(Headers headers, String taskId, String variableName) {
        String serviceToken = headers.getValue(SERVICE_AUTHORIZATION);
        Map<String, Object> body = Map.of(
            "variableName", variableName,
            "taskIdIn", singleton(taskId)
        );

        return camundaClient.searchHistory(
            serviceToken,
            body
        );
    }

    public Map<String, CamundaVariable> getTaskVariablesFromCamunda(Headers authenticationHeaders, String value) {

        return camundaApiActions.get(
                "process-instance/" + value + "/variables",
                authenticationHeaders
            ).then()
            .statusCode(200)
            .extract()
            .body()
            .jsonPath()
            .getMap("");

    }

    public Map<String, CamundaVariable> getTaskFromTaskManagementApi(Headers authenticationHeaders, String value) {
        String taskActionControllerEndPoint = "task/{task-id}";
        return taskManagementApiActions.get(
                taskActionControllerEndPoint,
                value,
                authenticationHeaders
            ).then()
            .extract()
            .body()
            .jsonPath()
            .getMap("");

    }

    private void clearAllRoleAssignmentsForUser(String userId, Headers headers) {
        String userToken = headers.getValue(AUTHORIZATION);
        String serviceToken = headers.getValue(SERVICE_AUTHORIZATION);

        RoleAssignmentResource response = null;

        try {
            //Retrieve All role assignments
            response = roleAssignmentServiceApi.getRolesForUser(userId, userToken, serviceToken);

        } catch (FeignException ex) {
            if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                log.info("No roles found, nothing to delete.");
            } else {
                ex.printStackTrace();
            }
        }

        if (response != null) {
            //Delete All role assignments
            List<RoleAssignment> organisationalRoleAssignments = response.getRoleAssignmentResponse().stream()
                .filter(assignment -> ORGANISATION.equals(assignment.getRoleType()))
                .toList();

            List<RoleAssignment> caseRoleAssignments = response.getRoleAssignmentResponse().stream()
                .filter(assignment -> CASE.equals(assignment.getRoleType()))
                .toList();

            //Check if there are 'orphaned' restricted roles
            if (organisationalRoleAssignments.isEmpty() && !caseRoleAssignments.isEmpty()) {
                log.info("Orphaned Restricted role assignments were found.");
                log.info("Creating a temporary role assignment to perform cleanup");
                //Create a temporary organisational role
                setupOrganisationalRoleAssignment(headers);
                //Recursive
                clearAllRoleAssignments(headers);
            }

            caseRoleAssignments.forEach(assignment ->
                                            roleAssignmentServiceApi.deleteRoleAssignmentById(
                                                assignment.getId(),
                                                userToken,
                                                serviceToken
                                            )
            );

            organisationalRoleAssignments.forEach(assignment ->
                                                      roleAssignmentServiceApi.deleteRoleAssignmentById(
                                                          assignment.getId(),
                                                          userToken,
                                                          serviceToken
                                                      )
            );
        }
    }

    private void createSupervisor(UserInfo userInfo, Headers headers) {
        log.info("Creating task supervisor organizational Role");

        postRoleAssignment(
            null,
            headers.getValue(AUTHORIZATION),
            headers.getValue(SERVICE_AUTHORIZATION),
            userInfo.getUid(),
            "task-supervisor",
            toJsonString(Map.of(
                "primaryLocation", "765324",
                "jurisdiction", "WA"
            )),
            "requests/roleAssignment/r2/set-organisational-role-assignment-request.json",
            "STANDARD",
            "LEGAL_OPERATIONS",
            toJsonString(List.of()),
            "ORGANISATION",
            "PUBLIC",
            "staff-organisational-role-mapping",
            userInfo.getUid(),
            false,
            false,
            null,
            "2020-01-01T00:00:00Z",
            null,
            userInfo.getUid()
        );
    }

    private void createTribunalCaseWorker(UserInfo userInfo, Headers headers, Map<String, String> attributes) {
        log.info("Creating Organizational Role");

        postRoleAssignment(
            null,
            headers.getValue(AUTHORIZATION),
            headers.getValue(SERVICE_AUTHORIZATION),
            userInfo.getUid(),
            "tribunal-caseworker",
            toJsonString(attributes),
            R2_ROLE_ASSIGNMENT_REQUEST,
            "STANDARD",
            "LEGAL_OPERATIONS",
            toJsonString(List.of()),
            "ORGANISATION",
            "PUBLIC",
            "staff-organisational-role-mapping",
            userInfo.getUid(),
            false,
            false,
            null,
            "2020-01-01T00:00:00Z",
            null,
            userInfo.getUid()
        );
    }

    private void createCaseAllocator(UserInfo userInfo, Headers headers) {
        log.info("Creating case allocator organizational Role");

        postRoleAssignment(
            null,
            headers.getValue(AUTHORIZATION),
            headers.getValue(SERVICE_AUTHORIZATION),
            userInfo.getUid(), "case-allocator",
            toJsonString(
                Map.of(
                    "primaryLocation", "765324",
                    "jurisdiction", "WA"
                )),
            R2_ROLE_ASSIGNMENT_REQUEST,
            "STANDARD",
            "LEGAL_OPERATIONS",
            toJsonString(List.of()),
            "ORGANISATION",
            "PUBLIC",
            "staff-organisational-role-mapping",
            userInfo.getUid(),
            false,
            false,
            null,
            "2020-01-01T00:00:00Z",
            null,
            userInfo.getUid()
        );
    }


    private void postRoleAssignment(String caseId,
                                    String bearerUserToken,
                                    String s2sToken,
                                    String actorId,
                                    String roleName,
                                    String attributes,
                                    String resourceFilename,
                                    String grantType,
                                    String roleCategory,
                                    String authorisations,
                                    String roleType,
                                    String classification,
                                    String process,
                                    String reference,
                                    boolean replaceExisting,
                                    Boolean readOnly,
                                    String notes,
                                    String beginTime,
                                    String endTime,
                                    String assignerId) {

        String body = getBody(caseId, actorId, roleName, resourceFilename, attributes, grantType, roleCategory,
                              authorisations, roleType, classification, process, reference, replaceExisting,
                              readOnly, notes, beginTime, endTime, assignerId
        );

        roleAssignmentServiceApi.createRoleAssignment(
            body,
            bearerUserToken,
            s2sToken
        );
    }

    private String getBody(final String caseId,
                           String actorId,
                           final String roleName,
                           final String resourceFilename,
                           final String attributes,
                           final String grantType,
                           String roleCategory,
                           String authorisations,
                           String roleType,
                           String classification,
                           String process,
                           String reference,
                           boolean replaceExisting,
                           Boolean readOnly,
                           String notes,
                           String beginTime,
                           String endTime,
                           String assignerId) {

        String assignmentRequestBody = null;

        try {
            assignmentRequestBody = FileUtils.readFileToString(ResourceUtils.getFile(
                "classpath:" + resourceFilename), StandardCharsets.UTF_8
            );
            assignmentRequestBody = assignmentRequestBody.replace("{ACTOR_ID_PLACEHOLDER}", actorId);
            assignmentRequestBody = assignmentRequestBody.replace("{ROLE_NAME_PLACEHOLDER}", roleName);
            assignmentRequestBody = assignmentRequestBody.replace("{GRANT_TYPE}", grantType);
            assignmentRequestBody = assignmentRequestBody.replace("{ROLE_CATEGORY}", roleCategory);
            assignmentRequestBody = assignmentRequestBody.replace("{ROLE_TYPE}", roleType);
            assignmentRequestBody = assignmentRequestBody.replace("{CLASSIFICATION}", classification);
            assignmentRequestBody = assignmentRequestBody.replace("{PROCESS}", process);
            assignmentRequestBody = assignmentRequestBody.replace("{ASSIGNER_ID_PLACEHOLDER}", assignerId);

            assignmentRequestBody = assignmentRequestBody.replace(
                "\"replaceExisting\": \"{REPLACE_EXISTING}\"",
                String.format("\"replaceExisting\": %s", replaceExisting)
            );

            if (beginTime != null) {
                assignmentRequestBody = assignmentRequestBody.replace(
                    "{BEGIN_TIME_PLACEHOLDER}",
                    beginTime
                );
            } else {
                assignmentRequestBody = assignmentRequestBody
                    .replace(",\n" + "      \"beginTime\": \"{BEGIN_TIME_PLACEHOLDER}\"", "");
            }

            if (endTime != null) {
                assignmentRequestBody = assignmentRequestBody.replace(
                    "{END_TIME_PLACEHOLDER}",
                    endTime
                );
            } else {
                assignmentRequestBody = assignmentRequestBody.replace(
                    "{END_TIME_PLACEHOLDER}",
                    ZonedDateTime.now(ZoneOffset.UTC).plusHours(2).format(ROLE_ASSIGNMENT_DATA_TIME_FORMATTER)
                );
            }

            if (attributes != null) {
                assignmentRequestBody = assignmentRequestBody
                    .replace("\"{ATTRIBUTES_PLACEHOLDER}\"", attributes);
            }

            if (caseId != null) {
                assignmentRequestBody = assignmentRequestBody.replace("{CASE_ID_PLACEHOLDER}", caseId);
            }

            assignmentRequestBody = assignmentRequestBody.replace("{REFERENCE}", reference);


            if (notes != null) {
                assignmentRequestBody = assignmentRequestBody.replace(
                    "\"notes\": \"{NOTES}\"",
                    String.format("\"notes\": [%s]", notes)
                );
            } else {
                assignmentRequestBody = assignmentRequestBody
                    .replace(",\n" + "      \"notes\": \"{NOTES}\"", "");
            }

            if (readOnly != null) {
                assignmentRequestBody = assignmentRequestBody.replace(
                    "\"readOnly\": \"{READ_ONLY}\"",
                    String.format("\"readOnly\": %s", readOnly)
                );
            } else {
                assignmentRequestBody = assignmentRequestBody
                    .replace(",\n" + "      \"readOnly\": \"{READ_ONLY}\"", "");
            }

            if (authorisations != null) {
                assignmentRequestBody = assignmentRequestBody.replace("\"{AUTHORISATIONS}\"", authorisations);
            } else {
                assignmentRequestBody = assignmentRequestBody
                    .replace(",\n" + "      \"authorisations\": \"{AUTHORISATIONS}\"", "");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return assignmentRequestBody;
    }

    private String toJsonString(Map<String, String> attributes) {
        String json = null;

        try {
            json = objectMapper.writeValueAsString(attributes);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }

    private String toJsonString(List<String> attributes) {
        String json = null;

        try {
            json = objectMapper.writeValueAsString(attributes);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }

    private Set<String> getProcesses(Headers authenticationHeaders, String caseId) {
        String filter = "/?variables=" + "caseId" + "_eq_" + caseId;
        List<String> processIds = camundaApiActions.get(
            "process-instance" + filter,
            authenticationHeaders
        ).then().extract().body().path("id");

        return Set.copyOf(processIds);
    }

    private void deleteProcessInstance(Headers authenticationHeaders, String processId) {
        String deleteRequest = DELETE_REQUEST.replace("{PROCESS_ID}", processId);
        camundaClient.deleteProcessInstance(authenticationHeaders.getValue(SERVICE_AUTHORIZATION), deleteRequest);
    }

    public void updateCftTaskState(Headers authenticationHeaders, String taskId) {
        String path = "task/" + taskId + "/localVariables";
        HashMap<String, CamundaValue<String>> camundaValueMap = new HashMap<>();
        HashMap<String, HashMap<String, CamundaValue<String>>> modifications = new HashMap<>();

        camundaValueMap.put("cftTaskState", stringValue("pendingTermination"));
        modifications.put("modifications", camundaValueMap);

        camundaApiActions.post(
            path,
            modifications,
            authenticationHeaders
        );

    }

}
