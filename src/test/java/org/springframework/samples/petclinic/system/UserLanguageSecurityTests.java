package org.springframework.samples.petclinic.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.samples.petclinic.config.SecurityConfig;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Users may change only their own language. Runs through the real security filter chain
 * from {@link SecurityConfig}.
 */
@WebMvcTest(UserController.class)
@Import({ SecurityConfig.class, UserLocaleResolver.class })
class UserLanguageSecurityTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserRepository users;

	@MockitoBean
	private LoginAuditService loginAuditService;

	@MockitoBean
	private CustomOAuth2UserService oauth2UserService;

	private final User alice = user(1L, "Alice");

	private final User bob = user(2L, "Bob");

	@BeforeEach
	void setUp() {
		when(users.findById(1L)).thenReturn(Optional.of(alice));
		when(users.findById(2L)).thenReturn(Optional.of(bob));
	}

	@Test
	void userCanChangeOwnLanguage() throws Exception {
		mockMvc.perform(post("/users/1/language").param("language", "cs").session(loggedIn(alice)))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/users/1"));

		verify(users).save(alice);
		assertThat(alice.getLanguage()).isEqualTo("cs");
	}

	@Test
	void userCannotChangeAnotherUsersLanguage() throws Exception {
		mockMvc.perform(post("/users/2/language").param("language", "cs").session(loggedIn(alice)))
			.andExpect(status().isForbidden());

		verify(users, never()).save(any());
		assertThat(bob.getLanguage()).isEqualTo("en");
	}

	@Test
	void anonymousUserIsSentToLogin() throws Exception {
		mockMvc.perform(post("/users/1/language").param("language", "cs")).andExpect(status().is3xxRedirection());

		verify(users, never()).save(any());
	}

	private static MockHttpSession loggedIn(User user) {
		CustomUserPrincipal principal = CustomUserPrincipal.create(user, Map.of());
		TestingAuthenticationToken auth = new TestingAuthenticationToken(principal, null,
				List.copyOf(principal.getAuthorities()));
		auth.setAuthenticated(true);
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
				new SecurityContextImpl(auth));
		return session;
	}

	private static User user(long id, String name) {
		User user = new User();
		user.setId(id);
		user.setName(name);
		user.setEmail(name.toLowerCase() + "@example.com");
		user.setLanguage("en");
		return user;
	}

}
