package org.cbioportal.genome_nexus.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private FirebaseAuthenticationProvider authenticationProvider;

    @Bean
    @Override
    public AuthenticationManager authenticationManager() throws Exception {
        return new ProviderManager(Arrays.asList(authenticationProvider));
    }


    public FirebaseAuthenticationTokenFilter authenticationTokenFilterBean() throws Exception {
        FirebaseAuthenticationTokenFilter authenticationTokenFilter = new FirebaseAuthenticationTokenFilter();
        authenticationTokenFilter.setAuthenticationManager(authenticationManager());
        authenticationTokenFilter.setAuthenticationSuccessHandler((request, response, authentication) -> {});
        return authenticationTokenFilter;
    }




    @Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring()
			.antMatchers(HttpMethod.OPTIONS);
	}


    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            // we don't need CSRF because our token is invulnerable
            .csrf().disable()
//            .cors()
//            .and()
            // All urls must be authenticated (filter for token always fires (/**)
            .authorizeRequests()
            .antMatchers(HttpMethod.OPTIONS).permitAll()
            .antMatchers("/version").authenticated()
            .antMatchers("/**").permitAll()
            .and()
            // don't create session
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) //.and()
            .and()
            .addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);


        // disable page caching
        // httpSecurity.headers().cacheControl();
    }
}
