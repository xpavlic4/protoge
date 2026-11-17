package org.springframework.samples.petclinic.cases;

import jakarta.persistence.*;
import org.springframework.samples.petclinic.system.Role;
import org.springframework.samples.petclinic.system.User;

/**
 * Role of a user within one case (judge, plaintiff or defendant).
 */
@Entity
@Table(name = "user_roles")
public class CaseRole {

	public static final String JUDGE = "ROLE_JUDGE";

	public static final String PLAINTIFF = "ROLE_PLAINTIFF";

	public static final String DEFENDANT = "ROLE_DEFENDANT";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "case_id")
	private Case aCase;

	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(optional = false)
	@JoinColumn(name = "role_id")
	private Role role;

	protected CaseRole() {
	}

	public CaseRole(Case aCase, User user, Role role) {
		this.aCase = aCase;
		this.user = user;
		this.role = role;
	}

	public Integer getId() {
		return id;
	}

	public Case getCase() {
		return aCase;
	}

	public User getUser() {
		return user;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

}
