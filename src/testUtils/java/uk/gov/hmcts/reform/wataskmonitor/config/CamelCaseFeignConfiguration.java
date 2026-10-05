package uk.gov.hmcts.reform.wataskmonitor.config;

import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson3.Jackson3Decoder;
import feign.jackson3.Jackson3Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

/**
 * Referenced only via {@code @FeignClient(configuration = ...)}.
 * Not a Spring {@code @Configuration}, so camelCase stays on the role-assignment client.
 */
public class CamelCaseFeignConfiguration {

    private final JsonMapper camelCaseMapper;

    @Autowired
    public CamelCaseFeignConfiguration(JsonMapper jsonMapper) {
        this.camelCaseMapper = jsonMapper.rebuild()
            .propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
            .build();
    }

    @Bean
    public Decoder feignDecoder() {
        return new Jackson3Decoder(camelCaseMapper);
    }

    @Bean
    public Encoder feignEncoder() {
        return new Jackson3Encoder(camelCaseMapper);
    }
}
