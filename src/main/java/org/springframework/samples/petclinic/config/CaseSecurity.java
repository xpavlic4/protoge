package org.springframework.samples.petclinic.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.cases.CaseRole;
import org.springframework.samples.petclinic.cases.CaseRoleRepository;
import org.springframework.samples.petclinic.system.CustomUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CaseSecurity {

	@Autowired
	private CaseRoleRepository caseRoleRepository;

	/**
	 * True when the user is the judge of the case (may edit it and manage parties).
	 */
	public boolean checkCaseOwner(Integer caseId, Authentication authentication) {
		return CaseRole.JUDGE.equals(roleOf(caseId, authentication));
	}

	/**
	 * True when the user has any role (judge, plaintiff, defendant) in the case.
	 */
	public boolean checkCaseAccess(Integer caseId, Authentication authentication) {
		return roleOf(caseId, authentication) != null;
	}

	private String roleOf(Integer caseId, Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()
				|| !(authentication.getPrincipal() instanceof CustomUserPrincipal principal)) {
			return null;
		}
		return caseRoleRepository.findByCaseIdAndUserId(caseId, principal.getUser().getId())
			.map(r -> r.getRole().getName())
			.orElse(null);
	}

}
