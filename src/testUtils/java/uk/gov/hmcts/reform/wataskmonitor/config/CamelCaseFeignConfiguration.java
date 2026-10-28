package uk.gov.hmcts.reform.wataskmonitor.config;

import feign.RequestTemplate;
import feign.Util;
import feign.codec.Decoder;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import feign.jackson3.Jackson3Decoder;
import feign.jackson3.Jackson3Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Type;

/**
 * Referenced only via {@code @FeignClient(configuration = ...)}.
 * Not a Spring {@code @Configuration}, so camelCase stays on the role-assignment client.
 * Raw {@link String}/{@code byte[]} bodies are passed through so template JSON is not
 * re-quoted by {@link Jackson3Encoder} (RoleAssignmentServiceApi posts a pre-built String).
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
        Encoder jackson3 = new Jackson3Encoder(camelCaseMapper);
        return new Encoder() {
            @Override
            public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
                if (bodyType == String.class) {
                    template.body(String.valueOf(object));
                    return;
                }
                if (bodyType == byte[].class) {
                    template.body((byte[]) object, null);
                    return;
                }
                if (object instanceof String) {
                    byte[] bodyData = ((String) object).getBytes(Util.UTF_8);
                    template.body(bodyData, Util.UTF_8);
                    return;
                }
                if (object instanceof byte[]) {
                    template.body((byte[]) object, null);
                    return;
                }
                jackson3.encode(object, bodyType, template);
            }
        };
    }
}
