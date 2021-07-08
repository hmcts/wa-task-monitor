package uk.gov.hmcts.reform.wataskmonitor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import uk.gov.hmcts.reform.wataskmonitor.Application;

import static java.util.Collections.singletonList;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .useDefaultResponseMessages(false)
            .select()
            .apis(RequestHandlerSelectors.basePackage(Application.class.getPackage().getName() + ".controllers"))
            .paths(PathSelectors.any())
            .build()
            .globalRequestParameters(singletonList(
                headerServiceAuthorization()
            ));
    }

    private RequestParameter headerServiceAuthorization() {
        return new RequestParameterBuilder()
            .name("ServiceAuthorization")
            .description("Keyword `Bearer` followed by a service-to-service token for a whitelisted micro-service")
            .in(ParameterType.HEADER)
            .required(true)
            .build();
    }

}
