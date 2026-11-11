package org.springframework.samples.petclinic.system;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.samples.petclinic.config.SecurityConfig;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The user list renders for any number of users; earlier, zero or one user led to
 * templates that do not exist.
 */
@WebMvcTest(UserController.class)
@Import({ SecurityConfig.class, UserLocaleResolver.class })
class UsersListPageTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserRepository users;

	@MockitoBean
	private LoginAuditService loginAuditService;

	@MockitoBean
	private CustomOAuth2UserService oauth2UserService;

	private final User admin = user(1L, "Alice");

	@Test
	void listsASingleUser() throws Exception {
		when(users.findAll(any(Pageable.class))).thenReturn(page(List.of(admin)));

		mockMvc.perform(get("/users").session(loggedIn(admin)))
			.andExpect(status().isOk())
			.andExpect(view().name("users/usersList"))
			.andExpect(content().string(containsString("alice@example.com")));
	}

	@Test
	void showsAnEmptyList() throws Exception {
		when(users.findAll(any(Pageable.class))).thenReturn(page(List.of()));

		mockMvc.perform(get("/users").session(loggedIn(admin)))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("No users.")));
	}

	@Test
	void pageBelowOneShowsFirstPage() throws Exception {
		when(users.findAll(any(Pageable.class))).thenReturn(page(List.of(admin)));

		mockMvc.perform(get("/users").param("page", "0").session(loggedIn(admin))).andExpect(status().isOk());
	}

	private static Page<User> page(List<User> content) {
		return new PageImpl<>(content, PageRequest.of(0, 5), content.size());
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

	private static User user(long id, String name) {
		User user = new User();
		user.setId(id);
		user.setName(name);
		user.setEmail(name.toLowerCase() + "@example.com");
		user.setLanguage("en");
		user.setAdmin(true);
		return user;
	}

}
