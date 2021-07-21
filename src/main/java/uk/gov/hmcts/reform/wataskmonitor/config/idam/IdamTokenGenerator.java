package uk.gov.hmcts.reform.wataskmonitor.config.idam;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.wataskmonitor.clients.IdamWebApi;
import uk.gov.hmcts.reform.wataskmonitor.domain.idam.Token;
import uk.gov.hmcts.reform.wataskmonitor.domain.idam.UserIdamTokenGeneratorInfo;
import uk.gov.hmcts.reform.wataskmonitor.domain.idam.UserInfo;

public class IdamTokenGenerator {

    private final UserIdamTokenGeneratorInfo userIdamTokenGeneratorInfo;
    private final IdamWebApi idamWebApi;

    public IdamTokenGenerator(UserIdamTokenGeneratorInfo userIdamTokenGeneratorInfo,
                              IdamWebApi idamWebApi) {
        this.userIdamTokenGeneratorInfo = userIdamTokenGeneratorInfo;
        this.idamWebApi = idamWebApi;
    }

    public String generate() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("redirect_uri", userIdamTokenGeneratorInfo.getIdamRedirectUrl());
        map.add("client_id", userIdamTokenGeneratorInfo.getIdamClientId());
        map.add("client_secret", userIdamTokenGeneratorInfo.getIdamClientSecret());
        map.add("username", userIdamTokenGeneratorInfo.getUserName());
        map.add("password", userIdamTokenGeneratorInfo.getUserPassword());
        map.add("scope", userIdamTokenGeneratorInfo.getIdamScope());
        Token tokenResponse = idamWebApi.token(map);

        return "Bearer " + tokenResponse.getAccessToken();
    }

    public UserInfo getUserInfo(String bearerAccessToken) {
        return idamWebApi.userInfo(bearerAccessToken);
    }
}
