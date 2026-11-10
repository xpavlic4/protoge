package org.springframework.samples.petclinic.config;

import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.petclinic.system.CustomOAuth2UserService;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService)
			throws Exception {
		http.authorizeHttpRequests(auth -> auth.requestMatchers(PathRequest.toStaticResources().atCommonLocations())
			.permitAll()
			.requestMatchers("/login", "/error", "/actuator/health", "/ping")
			.permitAll()
			.requestMatchers("/*.map")
			.permitAll()
			// 2. Safe static asset routing for your PetClinic styles
			.requestMatchers("/resources/css/**", "/resources/js/**", "/resources/fonts/**", "/resources/images/**")
			.permitAll()
			.requestMatchers("/favicon.ico")
			.permitAll()
			.requestMatchers("/target/petclinic.css.map")
			.permitAll()
			.anyRequest()
			.authenticated())
			.oauth2Login(oauth2Login -> oauth2Login.loginPage("/login")
				.defaultSuccessUrl("/")
				.failureUrl("/login?error=true")
				.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)))
			.csrf(c -> c.disable())
			.logout(c -> c.logoutUrl("/logout").invalidateHttpSession(true).permitAll());
		return http.build();
	}

}
