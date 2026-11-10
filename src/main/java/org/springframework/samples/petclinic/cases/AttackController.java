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

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.samples.petclinic.system.CustomUserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Optional;

@Controller
class AttackController {

	private final CaseRepository cases;

	private final ArgumentRepository arguments;

	public AttackController(CaseRepository cases, ArgumentRepository arguments) {
		this.cases = cases;
		this.arguments = arguments;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	/**
	 * Called before each and every @RequestMapping annotated method. 2 goals: - Make sure
	 * we always have fresh data - Since we do not use the session scope, make sure that
	 * Pet object always has an id (Even though id is not part of the form fields)
	 * @param argId
	 * @return Pet
	 */
	@ModelAttribute("attack")
	public Argument loadPetWithVisit(@AuthenticationPrincipal CustomUserPrincipal principal,
			@PathVariable("caseId") int caseId, @PathVariable("argumentId") int argId, Map<String, Object> model) {
		Optional<Case> optionalOwner = cases.findById(caseId);
		Case aCase = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Case not found with id: " + caseId + ". Please ensure the ID is correct "));

		Argument argument = findArgumentInCase(caseId, argId);
		model.put("argument", argument);
		model.put("case", aCase);

		Argument attack = new Argument();
		attack.setUser(principal.getUser());
		// argument.addAttack(attack);
		return attack;
	}

	// @ModelAttribute("case")
	// public Case findCase(@PathVariable("caseId") int caseId) {
	// Optional<Case> optionalCase = this.cases.findById(caseId);
	// Case aCase = optionalCase.orElseThrow(() -> new IllegalArgumentException(
	// "Case not found with id: " + caseId + ". Please ensure the ID is correct "));
	// return aCase;
	// }
	//
	// @ModelAttribute("argument")
	// public Argument findPet(@PathVariable("caseId") int caseId,
	// @PathVariable(name = "argumentId") Integer argumentId) {
	//
	// Optional<Case> optionalCase = this.cases.findById(caseId);
	// Case aCase = optionalCase.orElseThrow(() -> new IllegalArgumentException(
	// "Case not found with id: " + caseId + ". Please ensure the ID is correct "));
	// return aCase.getArgument(argumentId);
	// }

	// Spring MVC calls method loadPetWithVisit(...) before initNewVisitForm is
	// called
	@GetMapping("/cases/{caseId}/arguments/{argumentId}/rebutting/new")
	@PreAuthorize("@caseSecurity.checkCaseOwner(#caseId, authentication)")
	public String initNewVisitForm(@PathVariable("caseId") int caseId) {
		return "arguments/createOrUpdateAttackForm";
	}

	@GetMapping("/cases/{caseId}/arguments/{argumentId}/undercutting/new")
	@PreAuthorize("@caseSecurity.checkCaseOwner(#caseId, authentication)")
	public String initNewUndercuttingForm(@PathVariable("caseId") int caseId) {
		return "arguments/createOrUpdateAttackForm";
	}

	@GetMapping("/cases/{caseId}/arguments/{argumentId}/undermining/new")
	@PreAuthorize("@caseSecurity.checkCaseOwner(#caseId, authentication)")
	public String initNewUnderminingForm(@PathVariable("caseId") int caseId) {
		return "arguments/createOrUpdateAttackForm";
	}

	// Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is
	// called
	@PostMapping({ "/cases/{caseId}/arguments/{argumentId}/rebutting/new",
			"/cases/{caseId}/arguments/{argumentId}/undercutting/new",
			"/cases/{caseId}/arguments/{argumentId}/undermining/new" })
	@PreAuthorize("@caseSecurity.checkCaseOwner(#caseId, authentication)")
	@Transactional
	public String processNewVisitForm(@AuthenticationPrincipal CustomUserPrincipal principal,
			@PathVariable(name = "argumentId") Integer argumentId, @Valid Argument attack, BindingResult result,
			RedirectAttributes redirectAttributes, @PathVariable("caseId") int caseId) {
		if (result.hasErrors()) {
			return "arguments/createOrUpdateAttackForm";
		}
		Argument att = new Argument();

		Optional<Case> optionalOwner = cases.findById(caseId);
		Case aCase = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Case not found with id: " + caseId + ". Please ensure the ID is correct "));

		Argument parentArg = findArgumentInCase(caseId, argumentId);

		att.setCase(aCase);
		att.setParentId(parentArg.getId());
		att.setPredicate(attack.getPredicate());
		att.setPremise(attack.getPremise());
		att.setWarrant(attack.getWarrant());
		att.setUser(principal.getUser());
		// attack.setCase(aCase);
		// aCase.addArgument(attack);
		// this.cases.save(aCase);
		this.arguments.saveAndFlush(att);
		redirectAttributes.addFlashAttribute("message", "Added.");
		return "redirect:/cases/{caseId}";
	}

	/**
	 * Loads an argument only if it belongs to the given case. The case ownership check in
	 * {@code @PreAuthorize} only covers {@code caseId}, so without this an argument of
	 * another case could be attacked by changing {@code argumentId} in the URL.
	 */
	Argument findArgumentInCase(int caseId, int argumentId) {
		return arguments.findById(argumentId)
			.filter(argument -> argument.getCase() != null && argument.getCase().getId() == caseId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
					"Argument " + argumentId + " not found in case " + caseId));
	}

}
