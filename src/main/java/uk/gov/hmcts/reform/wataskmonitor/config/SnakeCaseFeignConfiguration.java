package uk.gov.hmcts.reform.wataskmonitor.config;

import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import feign.jackson3.Jackson3Decoder;
import feign.jackson3.Jackson3Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.json.JsonMapper;

/**
 * Referenced only via {@code @FeignClient(configuration = ...)}.
 * Not a Spring {@code @Configuration}, so these codecs stay on the IDAM client.
 * The token call is form-encoded; the JSON delegate is Jackson 3.
 */
public class SnakeCaseFeignConfiguration {

    private final JsonMapper jsonMapper;

    @Autowired
    public SnakeCaseFeignConfiguration(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Bean
    public Decoder feignDecoder() {
        return new Jackson3Decoder(jsonMapper);
    }

    @Bean
    public Encoder feignEncoder() {
        return new SpringFormEncoder(new Jackson3Encoder(jsonMapper));
    }
}
