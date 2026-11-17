package org.springframework.samples.petclinic.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.cases.Case;
import org.springframework.samples.petclinic.cases.CaseRole;
import org.springframework.samples.petclinic.cases.CaseRoleRepository;
import org.springframework.samples.petclinic.system.CustomUserPrincipal;
import org.springframework.samples.petclinic.system.Role;
import org.springframework.samples.petclinic.system.User;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

class CaseSecurityTests {

	private final CaseRoleRepository repo = mock(CaseRoleRepository.class);

	private final CaseSecurity security = new CaseSecurity();

	CaseSecurityTests() {
		ReflectionTestUtils.setField(security, "caseRoleRepository", repo);
	}

	@Test
	void judgeMayEditAndAccess() {
		Authentication auth = authWithRole(CaseRole.JUDGE);

		assertThat(security.checkCaseOwner(1, auth)).isTrue();
		assertThat(security.checkCaseAccess(1, auth)).isTrue();
	}

	@Test
	void partiesMayAccessButNotEdit() {
		for (String role : new String[] { CaseRole.PLAINTIFF, CaseRole.DEFENDANT }) {
			Authentication auth = authWithRole(role);

			assertThat(security.checkCaseOwner(1, auth)).isFalse();
			assertThat(security.checkCaseAccess(1, auth)).isTrue();
		}
	}

	@Test
	void userWithoutRoleIsDenied() {
		User user = new User();
		user.setId(9L);
		Authentication auth = auth(user);
		when(repo.findByCaseIdAndUserId(1, 9L)).thenReturn(Optional.empty());

		assertThat(security.checkCaseOwner(1, auth)).isFalse();
		assertThat(security.checkCaseAccess(1, auth)).isFalse();
	}

	private Authentication authWithRole(String roleName) {
		User user = new User();
		user.setId(2L);
		Role role = new Role();
		role.setName(roleName);
		when(repo.findByCaseIdAndUserId(1, 2L)).thenReturn(Optional.of(new CaseRole(new Case(), user, role)));
		return auth(user);
	}

	private Authentication auth(User user) {
		Authentication auth = mock(Authentication.class);
		CustomUserPrincipal principal = mock(CustomUserPrincipal.class);
		when(principal.getUser()).thenReturn(user);
		when(auth.isAuthenticated()).thenReturn(true);
		when(auth.getPrincipal()).thenReturn(principal);
		return auth;
	}

}
