package uk.gov.hmcts.reform.wataskmonitor.clients;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.wataskmonitor.domain.idam.Token;
import uk.gov.hmcts.reform.wataskmonitor.domain.idam.UserInfo;

import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wataskmonitor.clients.IdamWebApi.FeignConfiguration;

@FeignClient(
    name = "idam-web-api",
    url = "${idam.url}",
    configuration = FeignConfiguration.class
)
public interface IdamWebApi {
    @GetMapping(
        value = "/o/userinfo",
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE
    )
    UserInfo userInfo(@RequestHeader(AUTHORIZATION) String userToken);

    @PostMapping(
        value = "/o/token",
        produces = APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    Token token(@RequestBody Map<String, ?> form);

    @Configuration
    class FeignConfiguration {
        @Bean
        @Primary
        public Encoder feignFormEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
            return new SpringFormEncoder(new SpringEncoder(messageConverters));
        }
    }
}
