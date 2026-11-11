package org.springframework.samples.petclinic.system;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.lang.NonNull;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.samples.petclinic.config.SecurityConfig;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The menu links to the user list for admins only.
 */
@WebMvcTest(WelcomeController.class)
@Import({ SecurityConfig.class, UserLocaleResolver.class })
class UsersMenuTests {

	private static final String USERS_LINK = "href=\"/users\"";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserRepository users;

	@MockitoBean
	private CustomOAuth2UserService oauth2UserService;

	@Test
	void adminSeesUsersLink() throws Exception {
		mockMvc.perform(get("/").session(loggedIn(admin())))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(USERS_LINK)));
	}

	private static User admin() {
		return user(true);
	}

	@Test
	void nonAdminDoesNotSeeUsersLink() throws Exception {
		mockMvc.perform(get("/").session(loggedIn(regular())))
			.andExpect(status().isOk())
			.andExpect(content().string(not(containsString(USERS_LINK))));
	}

	private static User regular() {
		return user(false);
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

	private static User user(boolean admin) {
		User user = new User();
		user.setId(1L);
		user.setName("Alice");
		user.setEmail("alice@example.com");
		user.setLanguage("en");
		user.setAdmin(admin);
		return user;
	}

}
