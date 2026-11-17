package org.springframework.samples.petclinic.cases;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.samples.petclinic.system.Role;
import org.springframework.samples.petclinic.system.RoleRepository;
import org.springframework.samples.petclinic.system.User;
import org.springframework.samples.petclinic.system.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Assigns per-case roles. The creator of a case is its judge; the judge picks the
 * plaintiff and the defendant by email (the users must already exist).
 */
@Service
public class CaseRoleService {

	private final CaseRoleRepository caseRoles;

	private final RoleRepository roles;

	private final UserRepository users;

	public CaseRoleService(CaseRoleRepository caseRoles, RoleRepository roles, UserRepository users) {
		this.caseRoles = caseRoles;
		this.roles = roles;
		this.users = users;
	}

	/**
	 * Validates the party emails. Returns field name -> message key for every problem.
	 */
	public Map<String, String> validateParties(User judge, String plaintiffEmail, String defendantEmail) {
		Map<String, String> errors = new LinkedHashMap<>();
		User plaintiff = checkUser("plaintiffEmail", plaintiffEmail, judge, errors);
		User defendant = checkUser("defendantEmail", defendantEmail, judge, errors);
		if (plaintiff != null && defendant != null && plaintiff.getId().equals(defendant.getId())) {
			errors.put("defendantEmail", "role.duplicate");
		}
		return errors;
	}

	private User checkUser(String field, String email, User judge, Map<String, String> errors) {
		if (isBlank(email)) {
			return null;
		}
		Optional<User> user = users.findByEmail(email.trim());
		if (user.isEmpty()) {
			errors.put(field, "role.userNotFound");
			return null;
		}
		if (judge != null && judge.getId().equals(user.get().getId())) {
			errors.put(field, "role.isJudge");
			return null;
		}
		return user.get();
	}

	@Transactional
	public void assignInitial(Case aCase, User judge, String plaintiffEmail, String defendantEmail) {
		caseRoles.save(new CaseRole(aCase, judge, role(CaseRole.JUDGE)));
		setParty(aCase, CaseRole.PLAINTIFF, plaintiffEmail);
		setParty(aCase, CaseRole.DEFENDANT, defendantEmail);
	}

	@Transactional
	public void updateParties(Case aCase, String plaintiffEmail, String defendantEmail) {
		setParty(aCase, CaseRole.PLAINTIFF, plaintiffEmail);
		setParty(aCase, CaseRole.DEFENDANT, defendantEmail);
	}

	/** Email of the user holding the given role in the case, or "" when unassigned. */
	@Transactional(readOnly = true)
	public String emailOf(Integer caseId, String roleName) {
		return caseRoles.findByCaseIdAndRoleName(caseId, roleName).map(r -> r.getUser().getEmail()).orElse("");
	}

	private void setParty(Case aCase, String roleName, String email) {
		Optional<CaseRole> existing = caseRoles.findByCaseIdAndRoleName(aCase.getId(), roleName);
		if (isBlank(email)) {
			existing.ifPresent(caseRoles::delete);
			return;
		}
		User user = users.findByEmail(email.trim()).orElseThrow();
		if (existing.isPresent() && existing.get().getUser().getId().equals(user.getId())) {
			return;
		}
		existing.ifPresent(r -> {
			caseRoles.delete(r);
			caseRoles.flush(); // free the (user, case) slot / old row before inserting
		});
		// a user holds one role per case: drop any other role of this user (never judge)
		caseRoles.findByCaseIdAndUserId(aCase.getId(), user.getId()).ifPresent(r -> {
			caseRoles.delete(r);
			caseRoles.flush();
		});
		caseRoles.save(new CaseRole(aCase, user, role(roleName)));
	}

	private Role role(String name) {
		return roles.findByName(name).orElseThrow(() -> new IllegalStateException("Missing role " + name));
	}

	private static boolean isBlank(String s) {
		return s == null || s.isBlank();
	}

}
