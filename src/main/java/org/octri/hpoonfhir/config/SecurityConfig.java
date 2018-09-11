package org.octri.hpoonfhir.config;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity web) throws Exception {
    	// TODO: Figure out the correct settings.
        web.ignoring()
                .antMatchers(HttpMethod.GET,
                        "/**").antMatchers(HttpMethod.POST, "/**");
    }
}