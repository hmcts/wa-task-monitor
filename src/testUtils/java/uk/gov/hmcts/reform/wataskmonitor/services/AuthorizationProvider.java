package uk.gov.hmcts.reform.wataskmonitor.services;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskmonitor.clients.IdamWebApi;
import uk.gov.hmcts.reform.wataskmonitor.domain.idam.UserInfo;
import uk.gov.hmcts.reform.wataskmonitor.entities.RoleCode;
import uk.gov.hmcts.reform.wataskmonitor.entities.TestAccount;
import uk.gov.hmcts.reform.wataskmonitor.entities.TestAuthenticationCredentials;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.wataskmonitor.config.SecurityConfiguration.AUTHORIZATION;
import static uk.gov.hmcts.reform.wataskmonitor.config.SecurityConfiguration.SERVICE_AUTHORIZATION;

@Slf4j
@Service
public class AuthorizationProvider {

    private final Map<String, String> tokens = new ConcurrentHashMap<>();
    private final Map<String, UserInfo> userInfo = new ConcurrentHashMap<>();
    @Value("${idam.redirectUrl}") protected String idamRedirectUrl;
    @Value("${idam.scope}") protected String userScope;
    @Value("${spring.security.oauth2.client.registration.oidc.client-id}") protected String idamClientId;
    @Value("${spring.security.oauth2.client.registration.oidc.client-secret}") protected String idamClientSecret;
    @Value("${idam.test-account-pw}") protected String idamTestAccountPassword;

    @Autowired
    private IdamWebApi idamWebApi;

    @Autowired
    private IdamServiceApi idamServiceApi;

    @Autowired
    @Qualifier("authTokenGenerator")
    private AuthTokenGenerator serviceAuthTokenGenerator;

    @Value("${idam.test.userCleanupEnabled:false}")
    private boolean testUserDeletionEnabled;

    public void deleteAccount(String username) {

        if (testUserDeletionEnabled) {
            log.info("Deleting test account '{}'", username);
            idamServiceApi.deleteTestUser(username);
        } else {
            log.info("Test User deletion feature flag was not enabled, user '{}' was not deleted", username);
        }
    }

    public Header getServiceAuthorizationHeader() {
        String serviceToken = tokens.computeIfAbsent(
            SERVICE_AUTHORIZATION,
            user -> serviceAuthTokenGenerator.generate()
        );

        return new Header(SERVICE_AUTHORIZATION, serviceToken);
    }

    public TestAuthenticationCredentials getNewTribunalCaseworker(String emailPrefix) {
        /*
         * This user is used to assign role assignments to on a per test basis.
         * A clean up before assigning new role assignments is needed.
         */
        TestAccount caseworker = getIdamLawFirmCredentials(emailPrefix);

        Headers authenticationHeaders = new Headers(
            getAuthorizationOnly(caseworker),
            getServiceAuthorizationHeader()
        );

        return new TestAuthenticationCredentials(caseworker, authenticationHeaders);
    }

    public TestAuthenticationCredentials getNewLawFirm() {
        /*
         * This user is used to create cases in ccd
         */
        TestAccount lawfirm = getIdamLawFirmCredentials("wa-ft-lawfirm-");

        Headers authenticationHeaders = new Headers(
            getAuthorizationOnly(lawfirm),
            getServiceAuthorizationHeader()
        );

        return new TestAuthenticationCredentials(lawfirm, authenticationHeaders);
    }


    public Header getCaseworkerAuthorizationOnly(String emailPrefix) {
        TestAccount caseworker = getIdamCaseWorkerCredentials(emailPrefix);
        return getAuthorization(caseworker.getUsername(), caseworker.getPassword());

    }

    public Header getLawFirmAuthorizationOnly() {

        TestAccount lawfirm = getIdamLawFirmCredentials("wa-ft-lawfirm-");
        return getAuthorization(lawfirm.getUsername(), lawfirm.getPassword());

    }

    public Header getAuthorizationOnly(TestAccount account) {
        return getAuthorization(account.getUsername(), account.getPassword());
    }

    public UserInfo getUserInfo(String userToken) {
        return userInfo.computeIfAbsent(
            userToken,
            user -> idamWebApi.userInfo(userToken)
        );

    }

    public Headers getServiceAuthorizationHeadersOnly() {
        return new Headers(getServiceAuthorizationHeader());
    }

    private Header getAuthorization(String username, String password) {

        MultiValueMap<String, String> body = createIdamRequest(username, password);

        String accessToken = tokens.computeIfAbsent(
            username,
            user -> "Bearer " + idamWebApi.token(body).getAccessToken()
        );

        return new Header(AUTHORIZATION, accessToken);
    }

    private TestAccount getIdamCaseWorkerCredentials(String emailPrefix) {
        List<RoleCode> requiredRoles = asList(new RoleCode("caseworker-wa"), new RoleCode("caseworker-wa-caseofficer"));
        return generateIdamTestAccount(emailPrefix, requiredRoles);
    }

    private TestAccount getIdamLawFirmCredentials(String emailPrefix) {
        List<RoleCode> requiredRoles = asList(new RoleCode("caseworker-wa-task-configuration"),
            new RoleCode("payments"),
            new RoleCode("caseworker-wa"));
        return generateIdamTestAccount(emailPrefix, requiredRoles);
    }

    private MultiValueMap<String, String> createIdamRequest(String username, String password) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("redirect_uri", idamRedirectUrl);
        body.add("client_id", idamClientId);
        body.add("client_secret", idamClientSecret);
        body.add("username", username);
        body.add("password", password);
        body.add("scope", userScope);

        return body;
    }

    private TestAccount generateIdamTestAccount(String emailPrefix, List<RoleCode> requiredRoles) {
        String email = emailPrefix + UUID.randomUUID() + "@fake.hmcts.net";
        String tempPassword = idamTestAccountPassword;
        log.info("temppassword DELETE: ", tempPassword);

        log.info("Attempting to create a new test account {}", email);

        RoleCode userGroup = new RoleCode("caseworker");

        Map<String, Object> body = new ConcurrentHashMap<>();
        body.put("email", email);
        body.put("password", tempPassword);
        body.put("forename", "WAFTAccount");
        body.put("surname", "Functional");
        body.put("roles", requiredRoles);
        body.put("userGroup", userGroup);

        idamServiceApi.createTestUser(body);

        log.info("Test account created successfully");
        return new TestAccount(email, tempPassword);
    }
}
