package uk.gov.hmcts.reform.wataskmonitor.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskmonitor.clients.IdamWebApi2;
import uk.gov.hmcts.reform.wataskmonitor.domain.idam.UserInfo;

import static java.util.Objects.requireNonNull;

@CacheConfig(cacheNames = {"idamDetails"})
@Component
public class IdamService {

    private final IdamWebApi2 idamWebApi;

    @Autowired
    public IdamService(IdamWebApi2 idamWebApi) {
        this.idamWebApi = idamWebApi;
    }

    @Cacheable(value = "idam_user_info_cache", key = "#accessToken")
    public UserInfo getUserInfo(String accessToken) {
        requireNonNull(accessToken, "access token must not be null");
        return idamWebApi.userInfo(accessToken);
    }

    @Cacheable(value = "idam_user_id_cache", key = "#accessToken")
    public String getUserId(String accessToken) {
        UserInfo userInfo = getUserInfo(accessToken);
        requireNonNull(userInfo.getUid(), "User id must not be null");
        return userInfo.getUid();
    }
}
