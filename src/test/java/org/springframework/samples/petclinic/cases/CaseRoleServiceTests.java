package org.springframework.samples.petclinic.cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.system.Role;
import org.springframework.samples.petclinic.system.RoleRepository;
import org.springframework.samples.petclinic.system.User;
import org.springframework.samples.petclinic.system.UserRepository;

class CaseRoleServiceTests {

	private final CaseRoleRepository caseRoles = mock(CaseRoleRepository.class);

	private final RoleRepository roles = mock(RoleRepository.class);

	private final UserRepository users = mock(UserRepository.class);

	private final CaseRoleService service = new CaseRoleService(caseRoles, roles, users);

	private final User judge = user(1L, "judge@x.cz");

	@Test
	void unknownEmailIsRejected() {
		when(users.findByEmail("nobody@x.cz")).thenReturn(Optional.empty());

		assertThat(service.validateParties(judge, "nobody@x.cz", "")).containsEntry("plaintiffEmail",
				"role.userNotFound");
	}

	@Test
	void sameUserAsPlaintiffAndDefendantIsRejected() {
		when(users.findByEmail("a@x.cz")).thenReturn(Optional.of(user(2L, "a@x.cz")));

		assertThat(service.validateParties(judge, "a@x.cz", "a@x.cz")).containsEntry("defendantEmail",
				"role.duplicate");
	}

	@Test
	void judgeCannotBeAParty() {
		when(users.findByEmail("judge@x.cz")).thenReturn(Optional.of(judge));

		assertThat(service.validateParties(judge, "judge@x.cz", null)).containsEntry("plaintiffEmail", "role.isJudge");
	}

	@Test
	void blankPartiesAreValid() {
		assertThat(service.validateParties(judge, "", null)).isEmpty();
	}

	@Test
	void assignInitialCreatesJudgeAndParties() {
		Case aCase = new Case();
		aCase.setId(5);
		when(roles.findByName(any())).thenAnswer(i -> Optional.of(role(i.getArgument(0))));
		when(users.findByEmail("p@x.cz")).thenReturn(Optional.of(user(2L, "p@x.cz")));
		when(users.findByEmail("d@x.cz")).thenReturn(Optional.of(user(3L, "d@x.cz")));
		when(caseRoles.findByCaseIdAndRoleName(any(), any())).thenReturn(Optional.empty());
		when(caseRoles.findByCaseIdAndUserId(any(), any())).thenReturn(Optional.empty());

		service.assignInitial(aCase, judge, "p@x.cz", "d@x.cz");

		verify(caseRoles, org.mockito.Mockito.times(3)).save(any(CaseRole.class));
	}

	@Test
	void updateRemovesPartyWhenEmailBlank() {
		CaseRole existing = new CaseRole(new Case(), user(2L, "p@x.cz"), role(CaseRole.PLAINTIFF));
		when(caseRoles.findByCaseIdAndRoleName(5, CaseRole.PLAINTIFF)).thenReturn(Optional.of(existing));
		when(caseRoles.findByCaseIdAndRoleName(5, CaseRole.DEFENDANT)).thenReturn(Optional.empty());
		Case aCase = new Case();
		aCase.setId(5);

		service.updateParties(aCase, "", "");

		verify(caseRoles).delete(existing);
		verify(caseRoles, never()).save(any());
	}

	private static User user(Long id, String email) {
		User u = new User();
		u.setId(id);
		u.setEmail(email);
		return u;
	}

	private static Role role(String name) {
		Role r = new Role();
		r.setName(name);
		return r;
	}

}
