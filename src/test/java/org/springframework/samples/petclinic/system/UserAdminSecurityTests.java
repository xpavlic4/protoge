package org.springframework.samples.petclinic.system;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.samples.petclinic.config.SecurityConfig;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * User management under {@code /users} is for admins ({@code users.admin = true}) only.
 * Runs through the real security filter chain from {@link SecurityConfig}.
 */
@WebMvcTest(UserController.class)
@Import({ SecurityConfig.class, UserLocaleResolver.class })
class UserAdminSecurityTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserRepository users;

	@MockitoBean
	private LoginAuditService loginAuditService;

	@MockitoBean
	private CustomOAuth2UserService oauth2UserService;

	private final User admin = user(1L, "Alice", true);

	private final User regular = user(2L, "Bob", false);

	@Test
	void adminCanListUsers() throws Exception {
		when(users.findByNameStartingWith(anyString(), any()))
			.thenReturn(new PageImpl<>(List.of(admin, regular), PageRequest.of(0, 5), 2));

		mockMvc.perform(get("/users").session(loggedIn(admin))).andExpect(status().isOk());
	}

	@Test
	void nonAdminCannotListUsers() throws Exception {
		mockMvc.perform(get("/users").session(loggedIn(regular))).andExpect(status().isForbidden());
	}

	@Test
	void nonAdminCannotOpenUserSearch() throws Exception {
		mockMvc.perform(get("/users/find").session(loggedIn(regular))).andExpect(status().isForbidden());
	}

	@Test
	void nonAdminCannotViewAnotherUser() throws Exception {
		mockMvc.perform(get("/users/1").session(loggedIn(regular))).andExpect(status().isForbidden());
	}

	@Test
	void anonymousUserIsSentToLogin() throws Exception {
		mockMvc.perform(get("/users")).andExpect(status().is3xxRedirection());
	}

	private static MockHttpSession loggedIn(User user) {
		CustomUserPrincipal principal = CustomUserPrincipal.create(user, Map.of("name", user.getName()));
		TestingAuthenticationToken auth = new TestingAuthenticationToken(principal, null,
				List.copyOf(principal.getAuthorities()));
		auth.setAuthenticated(true);
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
				new SecurityContextImpl(auth));
		return session;
	}

	private static User user(long id, String name, boolean admin) {
		User user = new User();
		user.setId(id);
		user.setName(name);
		user.setEmail(name.toLowerCase() + "@example.com");
		user.setLanguage("en");
		user.setAdmin(admin);
		return user;
	}

}
