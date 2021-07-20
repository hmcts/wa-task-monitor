package uk.gov.hmcts.reform.wataskmonitor.domain.idam;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Getter
public class Token {

    private String accessToken;
    private String scope;

    private Token() {
        //No-op constructor for deserialization
    }

    public Token(String accessToken, String scope) {
        this.accessToken = accessToken;
        this.scope = scope;
    }

}
