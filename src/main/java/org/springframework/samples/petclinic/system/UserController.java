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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
class UserController {

	private final UserRepository users;

	private final LoginAuditService loginAuditService;

	public UserController(UserRepository users, LoginAuditService loginAuditService) {
		this.users = users;
		this.loginAuditService = loginAuditService;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@ModelAttribute("user")
	public User findOwner(@PathVariable(name = "userId", required = false) Long userId) {
		return userId == null ? new User()
				: this.users.findById(userId)
					.orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId
							+ ". Please ensure the ID is correct " + "and the case exists in the database."));
	}

	@GetMapping("/users/find")
	public String initFindForm() {
		return "users/findUsers";
	}

	/**
	 * Custom handler for displaying an owner.
	 * @param userId the ID of the user to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/users/{userId}")
	public ModelAndView showCase(@PathVariable("userId") long userId) {
		ModelAndView mav = new ModelAndView("users/userDetails");
		Optional<User> optionalCase = this.users.findById(userId);
		User aCase = optionalCase.orElseThrow(() -> new IllegalArgumentException(
				"User not found with id: " + userId + ". Please ensure the ID is correct "));
		mav.addObject(aCase);
		return mav;
	}

	/**
	 * Cases
	 */

	@GetMapping("/users")
	public String processFindForm(@RequestParam(defaultValue = "1") int page, User anUser, BindingResult result,
			Model model) {
		// allow parameterless GET request for /owners to return all records
		if (anUser.getName() == null) {
			anUser.setName(""); // empty string signifies broadest possible search
		}

		// find owners by last name
		Page<User> casesResult = findPaginatedForCasesLastName(page, anUser.getName());
		if (casesResult.isEmpty()) {
			// no owners found
			result.rejectValue("name", "notFound", "not found");
			return "users/findUsers";
		}

		if (casesResult.getTotalElements() == 1) {
			// 1 case found
			anUser = casesResult.iterator().next();
			return "redirect:/users/" + anUser.getId();
		}

		// multiple owners found
		return addPaginationModelCases(page, model, casesResult);
	}

	private String addPaginationModelCases(int page, Model model, Page<User> paginated) {
		List<User> listUsers = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listUsers", listUsers);

		Map<String, String> lastLoginTimes = listUsers.stream()
			.map(User::getEmail)
			.collect(Collectors.toMap(email -> email,
					email -> TimeAgoFormatter.format(loginAuditService.getLastLoginTime(email).orElse(null))));
		model.addAttribute("lastLoginTimes", lastLoginTimes);

		return "users/usersList";
	}

	private Page<User> findPaginatedForCasesLastName(int page, String lastname) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return users.findByNameStartingWith(lastname, pageable);
	}

	@PostMapping("/users/{userId}/language")
	public String updateUserLanguage(@PathVariable("userId") Long userId, @RequestParam("language") String language,
			@AuthenticationPrincipal CustomUserPrincipal principal) {
		// Users may change only their own language.
		if (principal == null || !userId.equals(principal.getUser().getId())) {
			throw new AccessDeniedException("You can only change your own language.");
		}
		User user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + userId));
		user.setLanguage(language);
		users.save(user);
		return "redirect:/users/" + userId;
	}

}
