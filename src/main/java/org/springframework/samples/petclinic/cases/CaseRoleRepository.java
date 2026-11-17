package org.springframework.samples.petclinic.cases;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CaseRoleRepository extends JpaRepository<CaseRole, Integer> {

	@Query("SELECT r FROM CaseRole r WHERE r.aCase.id = :caseId")
	List<CaseRole> findByCaseId(Integer caseId);

	@Query("SELECT r FROM CaseRole r WHERE r.aCase.id = :caseId AND r.user.id = :userId")
	Optional<CaseRole> findByCaseIdAndUserId(Integer caseId, Long userId);

	@Query("SELECT r FROM CaseRole r WHERE r.aCase.id = :caseId AND r.role.name = :roleName")
	Optional<CaseRole> findByCaseIdAndRoleName(Integer caseId, String roleName);

}
