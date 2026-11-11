package org.springframework.samples.petclinic.config;

import java.util.function.Supplier;

import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.petclinic.system.CustomOAuth2UserService;
import org.springframework.samples.petclinic.system.CustomUserPrincipal;
import org.springframework.samples.petclinic.system.User;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

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
			// User management: admins only (users.admin = true).
			.requestMatchers("/users", "/users/**")
			.access(admin())
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

	private static AuthorizationManager<RequestAuthorizationContext> admin() {
		return (authentication, context) -> {
			User user = currentUser(authentication);
			return new AuthorizationDecision(user != null && user.isAdmin());
		};
	}

	private static User currentUser(Supplier<? extends Authentication> authentication) {
		Authentication auth = authentication.get();
		if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserPrincipal principal) {
			return principal.getUser();
		}
		return null;
	}

}
