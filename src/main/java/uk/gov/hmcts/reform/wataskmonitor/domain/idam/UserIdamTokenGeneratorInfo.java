package uk.gov.hmcts.reform.wataskmonitor.domain.idam;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Builder
@Getter
public class UserIdamTokenGeneratorInfo {

    private final String userName;
    private final String userPassword;
    private final String idamRedirectUrl;
    private final String idamScope;
    private final String idamClientId;
    private final String idamClientSecret;

}
