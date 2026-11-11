package org.springframework.samples.petclinic.cases;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.samples.petclinic.system.CustomUserPrincipal;
import org.springframework.samples.petclinic.system.User;
import org.springframework.samples.petclinic.system.UserLocaleResolver;
import org.springframework.samples.petclinic.system.UserRepository;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Renders the argument detail page. Access checks are covered elsewhere; security filters
 * are off here.
 */
@WebMvcTest(ArgumentsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(UserLocaleResolver.class)
class ArgumentDetailsPageTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CaseRepository cases;

	@MockitoBean
	private ArgumentRepository arguments;

	@MockitoBean
	private UserRepository users;

	@Test
	void evaluationDeleteLinkOfDeepestAttackPointsAtThatAttack() throws Exception {
		// The page shows three levels of attacks under the argument (arg, attack, att2):
		// argument 10 <- 11 <- 12 <- 13, and attack 13 has evaluation 30.
		Case aCase = new Case();
		aCase.setId(1);
		Argument argument = argument(10, aCase);
		Argument level1 = argument(11, aCase);
		Argument level2 = argument(12, aCase);
		Argument level3 = argument(13, aCase);
		Evaluation evaluation = new Evaluation();
		evaluation.setId(30);
		level3.addEvaluation(evaluation);
		argument.getAttacks().add(level1);
		level1.getAttacks().add(level2);
		level2.getAttacks().add(level3);
		aCase.addArgument(argument);
		when(cases.findById(1)).thenReturn(Optional.of(aCase));
		when(cases.findArgumentTypes()).thenReturn(List.of());
		when(cases.findASourceTypes()).thenReturn(List.of());

		mockMvc.perform(get("/cases/1/arguments/10/detail").principal(loggedIn()))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("/cases/1/arguments/13/evaluations/30/delete")))
			.andExpect(content().string(not(containsString("/cases/1/arguments/12/evaluations/30/delete"))));
	}

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	/** The layout shows the logged-in user's name, so the page needs a principal. */
	private static TestingAuthenticationToken loggedIn() {
		User user = new User();
		user.setId(1L);
		user.setName("Alice");
		user.setEmail("alice@example.com");
		user.setLanguage("en");
		TestingAuthenticationToken auth = new TestingAuthenticationToken(
				CustomUserPrincipal.create(user, Map.of("name", "Alice")), null, List.of());
		auth.setAuthenticated(true);
		SecurityContextHolder.getContext().setAuthentication(auth);
		return auth;
	}

	private static Argument argument(int id, Case aCase) {
		Argument argument = new Argument();
		argument.setId(id);
		argument.setCase(aCase);
		argument.setPredicate("predicate " + id);
		argument.setPremise("premise " + id);
		return argument;
	}

}
