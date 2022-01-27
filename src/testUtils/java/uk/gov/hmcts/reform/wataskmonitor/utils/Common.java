package uk.gov.hmcts.reform.wataskmonitor.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.restassured.http.Headers;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.reform.wataskmonitor.clients.CamundaClient;
import uk.gov.hmcts.reform.wataskmonitor.config.GivensBuilder;
import uk.gov.hmcts.reform.wataskmonitor.config.RestApiActions;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskmonitor.domain.camunda.CamundaVariable;
import uk.gov.hmcts.reform.wataskmonitor.domain.idam.UserInfo;
import uk.gov.hmcts.reform.wataskmonitor.entities.RoleAssignment;
import uk.gov.hmcts.reform.wataskmonitor.entities.RoleAssignmentResource;
import uk.gov.hmcts.reform.wataskmonitor.entities.TestVariables;
import uk.gov.hmcts.reform.wataskmonitor.services.AuthorizationHeadersProvider;
import uk.gov.hmcts.reform.wataskmonitor.services.IdamService;
import uk.gov.hmcts.reform.wataskmonitor.services.RoleAssignmentServiceApi;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.hmcts.reform.wataskmonitor.config.SecurityConfiguration.AUTHORIZATION;
import static uk.gov.hmcts.reform.wataskmonitor.config.SecurityConfiguration.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.wataskmonitor.entities.enums.RoleType.CASE;
import static uk.gov.hmcts.reform.wataskmonitor.entities.enums.RoleType.ORGANISATION;

@Slf4j
public class Common {

    public static final DateTimeFormatter CAMUNDA_DATA_TIME_FORMATTER = ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
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
    private final RestApiActions restApiActions;
    private final RestApiActions camundaApiActions;
    private final RestApiActions taskManagementApiActions;
    private final AuthorizationHeadersProvider authorizationHeadersProvider;

    private final IdamService idamService;
    private final RoleAssignmentServiceApi roleAssignmentServiceApi;
    private final CamundaClient camundaClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Common(GivensBuilder given,
                  RestApiActions restApiActions,
                  RestApiActions camundaApiActions,
                  AuthorizationHeadersProvider authorizationHeadersProvider,
                  IdamService idamService,
                  RoleAssignmentServiceApi roleAssignmentServiceApi,
                  CamundaClient camundaClient,
                  RestApiActions taskManagementApiActions) {
        this.given = given;
        this.restApiActions = restApiActions;
        this.camundaApiActions = camundaApiActions;
        this.authorizationHeadersProvider = authorizationHeadersProvider;
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

        UserInfo userInfo = authorizationHeadersProvider.getUserInfo(headers.getValue(AUTHORIZATION));

        Map<String, String> attributes = Map.of(
            "primaryLocation", "765324",
            "region", "1",
            //This value must match the camunda task location variable for the permission check to pass
            "baseLocation", "765324",
            "jurisdiction", "IA"
        );

        //Clean/Reset user
        clearAllRoleAssignmentsForUser(userInfo.getUid(), headers);

        //Creates an organizational role for jurisdiction IA
        log.info("Creating Organizational Role");
        postRoleAssignment(
            null,
            headers.getValue(AUTHORIZATION),
            headers.getValue(SERVICE_AUTHORIZATION),
            userInfo,
            "tribunal-caseworker",
            toJsonString(attributes),
            "requests/roleAssignment/set-organisational-role-assignment-request.json"
        );

    }

    public void setupCftOrganisationalRoleAssignment(Headers headers) {
        UserInfo userInfo = authorizationHeadersProvider.getUserInfo(headers.getValue(AUTHORIZATION));

        Map<String, String> attributes = Map.of(
            "primaryLocation", "765324",
            "region", "1",
            //This value must match the camunda task location variable for the permission check to pass
            "baseLocation", "765324",
            "jurisdiction", "IA"
        );

        //Clean/Reset user
        clearAllRoleAssignmentsForUser(userInfo.getUid(), headers);

        //Creates an organizational role for jurisdiction IA
        log.info("Creating Organizational Role");

        postRoleAssignment(
            null,
            headers.getValue(AUTHORIZATION),
            headers.getValue(SERVICE_AUTHORIZATION),
            userInfo,
            "tribunal-caseworker",
            toJsonString(attributes),
            "requests/roleAssignment/r2/set-organisational-role-assignment-request.json"
        );

    }

    public void setupOrganisationalRoleAssignmentWithCustomAttributes(Headers headers, Map<String, String> attributes) {

        UserInfo userInfo = idamService.getUserInfo(headers.getValue(AUTHORIZATION));

        //Clean/Reset user
        clearAllRoleAssignmentsForUser(userInfo.getUid(), headers);

        //Creates an organizational role for jurisdiction IA
        log.info("Creating Organizational Role");
        postRoleAssignment(
            null,
            headers.getValue(AUTHORIZATION),
            headers.getValue(SERVICE_AUTHORIZATION),
            userInfo,
            "tribunal-caseworker",
            toJsonString(attributes),
            "requests/roleAssignment/set-organisational-role-assignment-request.json"
        );
    }

    public void clearAllRoleAssignments(Headers headers) {
        UserInfo userInfo = idamService.getUserInfo(headers.getValue(AUTHORIZATION));
        clearAllRoleAssignmentsForUser(userInfo.getUid(), headers);
    }


    public void cleanUpTask(Headers authenticationHeaders, List<String> values) {

        Set<String> processIds = new HashSet<>();

        values
            .forEach(value -> processIds.addAll(getProcesses(authenticationHeaders, value)));

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
                .collect(toList());

            List<RoleAssignment> caseRoleAssignments = response.getRoleAssignmentResponse().stream()
                .filter(assignment -> CASE.equals(assignment.getRoleType()))
                .collect(toList());

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
                roleAssignmentServiceApi.deleteRoleAssignmentById(assignment.getId(), userToken, serviceToken)
            );

            organisationalRoleAssignments.forEach(assignment ->
                roleAssignmentServiceApi.deleteRoleAssignmentById(assignment.getId(), userToken, serviceToken)
            );
        }
    }

    private void postRoleAssignment(String caseId,
                                    String bearerUserToken,
                                    String s2sToken,
                                    UserInfo userInfo,
                                    String roleName,
                                    String attributes,
                                    String resourceFilename) {

        try {
            roleAssignmentServiceApi.createRoleAssignment(
                getBody(caseId, userInfo, roleName, resourceFilename, attributes),
                bearerUserToken,
                s2sToken
            );
        } catch (FeignException ex) {
            ex.printStackTrace();
        }
    }

    private String getBody(final String caseId,
                           final UserInfo userInfo,
                           final String roleName,
                           final String resourceFilename,
                           final String attributes) {
        String assignmentRequestBody = null;
        try {
            assignmentRequestBody = FileUtils.readFileToString(ResourceUtils.getFile(
                "classpath:" + resourceFilename), "UTF-8"
            );
            assignmentRequestBody = assignmentRequestBody.replace("{ACTOR_ID_PLACEHOLDER}", userInfo.getUid());
            assignmentRequestBody = assignmentRequestBody.replace("{ASSIGNER_ID_PLACEHOLDER}", userInfo.getUid());
            assignmentRequestBody = assignmentRequestBody.replace("{ROLE_NAME_PLACEHOLDER}", roleName);
            if (attributes != null) {
                assignmentRequestBody = assignmentRequestBody.replace("\"{ATTRIBUTES_PLACEHOLDER}\"", attributes);
            }
            if (caseId != null) {
                assignmentRequestBody = assignmentRequestBody.replace("{CASE_ID_PLACEHOLDER}", caseId);

            }

            return assignmentRequestBody;
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

    private Set<String> getProcesses(Headers authenticationHeaders, String value) {
        String filter = "/?variables=" + "caseId" + "_eq_" + value;
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

}
