package org.springframework.samples.petclinic.system;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomUserPrincipal implements OAuth2User, UserDetails {

	private final User user; // Your DB entity

	private Map<String, Object> attributes;

	public CustomUserPrincipal(User user) {
		this.user = user;
	}

	public static CustomUserPrincipal create(User user, Map<String, Object> attributes) {
		CustomUserPrincipal principal = new CustomUserPrincipal(user);
		principal.setAttributes(attributes);
		return principal;
	}

	public User getUser() {
		return user;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// roles are per case (see CaseRole); global admin is users.admin
		return List.of();
	}

	@Override
	public String getPassword() {
		return null; // Not used for OAuth2
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getName() {
		return user.getName();
	}

}
