/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.system;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Admin-only list of all users with their last login.
 */
@Controller
class UserController {

	private static final int PAGE_SIZE = 5;

	private final UserRepository users;

	private final LoginAuditService loginAuditService;

	public UserController(UserRepository users, LoginAuditService loginAuditService) {
		this.users = users;
		this.loginAuditService = loginAuditService;
	}

	@GetMapping("/users")
	public String listUsers(@RequestParam(defaultValue = "1") int page, Model model) {
		int currentPage = Math.max(page, 1);
		Page<User> paginated = users.findAll(PageRequest.of(currentPage - 1, PAGE_SIZE, Sort.by("name", "id")));
		List<User> listUsers = paginated.getContent();
		model.addAttribute("currentPage", currentPage);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listUsers", listUsers);

		Map<String, String> lastLoginTimes = listUsers.stream()
			.map(User::getEmail)
			.distinct()
			.collect(Collectors.toMap(email -> email,
					email -> TimeAgoFormatter.format(loginAuditService.getLastLoginTime(email).orElse(null))));
		model.addAttribute("lastLoginTimes", lastLoginTimes);

		return "users/usersList";
	}

}
