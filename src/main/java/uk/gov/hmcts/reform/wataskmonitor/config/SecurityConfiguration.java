package uk.gov.hmcts.reform.wataskmonitor.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;


@Configuration
@ConfigurationProperties(prefix = "security")
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    public static final String AUTHORIZATION = "Authorization";
    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final List<String> anonymousPaths = new ArrayList<>();
    private final ServiceAuthFilter serviceAuthFilter;

    @Autowired
    public SecurityConfiguration(final ServiceAuthFilter serviceAuthFilter) {
        super();
        this.serviceAuthFilter = serviceAuthFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(serviceAuthFilter, AbstractPreAuthenticatedProcessingFilter.class)
            .sessionManagement().sessionCreationPolicy(STATELESS)
            .and()
            .httpBasic().disable()
            .formLogin().disable()
            .logout().disable()
            .csrf().disable();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().mvcMatchers(
            anonymousPaths.toArray(String[]::new)
        );
    }

    public List<String> getAnonymousPaths() {
        return anonymousPaths;
    }
}
