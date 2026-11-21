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
package org.springframework.samples.petclinic.cases;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import org.springframework.samples.petclinic.system.CustomUserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cases/{caseId}")
class ArgumentsController {

	private static final String VIEWS_ARGUMENTS_CREATE_OR_UPDATE_FORM = "arguments/createOrUpdateArgumentForm";

	private static final String VIEWS_ARGUMENTS_DETAIL = "arguments/argumentDetails";

	private final CaseRepository cases;

	private final ArgumentRepository arguments;

	public ArgumentsController(CaseRepository cases, ArgumentRepository arguments) {
		this.cases = cases;
		this.arguments = arguments;
	}

	@ModelAttribute("types")
	public Collection<ArgumentType> populatePetTypes() {
		return this.cases.findArgumentTypes();
	}

	@ModelAttribute("source_types")
	public Collection<SourceType> populateSourceTypes() {
		return this.cases.findASourceTypes();
	}

	@ModelAttribute("case")
	public Case findCase(@PathVariable("caseId") int caseId) {
		Optional<Case> optionalCase = this.cases.findById(caseId);
		Case aCase = optionalCase.orElseThrow(() -> new IllegalArgumentException(
				"Case not found with id: " + caseId + ". Please ensure the ID is correct "));
		return aCase;
	}

	@ModelAttribute("argument")
	public Argument findPet(@AuthenticationPrincipal CustomUserPrincipal principal, @PathVariable("caseId") int caseId,
			@PathVariable(name = "argumentId", required = false) Integer argumentId) {

		if (argumentId == null) {
			Argument argument = new Argument();
			argument.setUser(principal.getUser());
			return argument;
		}

		Optional<Case> optionalCase = this.cases.findById(caseId);
		Case aCase = optionalCase.orElseThrow(() -> new IllegalArgumentException(
				"Case not found with id: " + caseId + ". Please ensure the ID is correct "));
		return aCase.getArgument(argumentId);
	}

	@InitBinder("case")
	public void initCaseBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id", "*.id");
	}

	@InitBinder("argument")
	public void initPetBinder(WebDataBinder dataBinder) {
		dataBinder.setValidator(new ArgumentValidator());
	}

	@GetMapping("/arguments/new")
	@PreAuthorize("@caseSecurity.checkCaseAccess(#caseId, authentication)")
	public String initCreationForm(Case aCase, ModelMap model, @PathVariable("caseId") int caseId) {
		Argument pet = new Argument();
		aCase.addArgument(pet);
		return VIEWS_ARGUMENTS_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/arguments/new")
	@PreAuthorize("@caseSecurity.checkCaseAccess(#caseId, authentication)")
	public String processCreationForm(Case aCase, @Valid Argument argument, BindingResult result,
			RedirectAttributes redirectAttributes, @PathVariable("caseId") int caseId) {
		// if (StringUtils.hasText(argument.getName()) && argument.isNew() &&
		// aCase.getPet(argument.getName(), true) != null) {
		// result.rejectValue("name", "duplicate", "already exists");
		// }

		// if (StringUtils.hasText(argument.getName()) && argument.isNew()
		// && aCase.getArgument(argument.getName(), true) != null)
		// result.rejectValue("name", "duplicate", "already exists");

		LocalDate currentDate = LocalDate.now();
		// if (argument.getBirthDate() != null &&
		// argument.getBirthDate().isAfter(currentDate)) {
		// result.rejectValue("birthDate", "typeMismatch.birthDate");
		// }

		if (result.hasErrors()) {
			return VIEWS_ARGUMENTS_CREATE_OR_UPDATE_FORM;
		}

		aCase.addArgument(argument);
		this.cases.save(aCase);
		redirectAttributes.addFlashAttribute("message", "Added.");
		return "redirect:/cases/{caseId}";
	}

	@GetMapping("/arguments/{argumentId}/edit")
	@PreAuthorize("@caseSecurity.checkCaseAccess(#caseId, authentication)")
	public String initUpdateForm(@PathVariable("caseId") int caseId) {
		return VIEWS_ARGUMENTS_CREATE_OR_UPDATE_FORM;
	}

	@GetMapping("/arguments/{argumentId}/detail")
	@PreAuthorize("@caseSecurity.checkCaseAccess(#caseId, authentication)")
	public String initDetailForm(@PathVariable("caseId") int caseId) {
		return VIEWS_ARGUMENTS_DETAIL;
	}

	@PostMapping("/arguments/{argumentId}/edit")
	@PreAuthorize("@caseSecurity.checkCaseAccess(#caseId, authentication)")
	public String processUpdateForm(Case aCase, @Valid Argument argument,
			@PathVariable(name = "argumentId") Integer argumentId, BindingResult result,
			RedirectAttributes redirectAttributes, @AuthenticationPrincipal CustomUserPrincipal principal,
			@PathVariable("caseId") int caseId) {

		// String petName = argument.getName();

		// checking if the argument name already exist for the aCase
		// if (StringUtils.hasText(petName)) {
		// Pet existingPet = aCase.getPet(petName.toLowerCase(), false);
		// if (existingPet != null && existingPet.getId() != argument.getId()) {
		// result.rejectValue("name", "duplicate", "already exists");
		// }
		// }

		// LocalDate currentDate = LocalDate.now();
		// if (argument.getBirthDate() != null &&
		// argument.getBirthDate().isAfter(currentDate)) {
		// result.rejectValue("birthDate", "typeMismatch.birthDate");
		// }

		if (result.hasErrors()) {
			return VIEWS_ARGUMENTS_CREATE_OR_UPDATE_FORM;
		}
		argument.setUser(principal.getUser());
		argument.setId(argumentId);
		this.arguments.save(argument);
		redirectAttributes.addFlashAttribute("message", "Edited.");
		return "redirect:/cases/{caseId}";
	}

}
