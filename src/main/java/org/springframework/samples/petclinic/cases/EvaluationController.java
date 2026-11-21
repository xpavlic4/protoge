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

import java.util.*;

import org.springframework.samples.petclinic.system.CustomUserPrincipal;
import org.springframework.samples.petclinic.system.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
class EvaluationController {

	private final CaseRepository cases;

	public EvaluationController(CaseRepository cases) {
		this.cases = cases;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id", "*.id");
	}

	/**
	 * Called before each and every @RequestMapping annotated method. 2 goals: - Make sure
	 * we always have fresh data - Since we do not use the session scope, make sure that
	 * Pet object always has an id (Even though id is not part of the form fields)
	 * @param argumentId
	 * @return Pet
	 */
	@ModelAttribute("evaluation")
	public Evaluation loadPetWithVisit(@AuthenticationPrincipal CustomUserPrincipal principal,
			@PathVariable("caseId") int caseId, @PathVariable("argumentId") int argumentId, Map<String, Object> model) {
		Optional<Case> optionalCase = cases.findById(caseId);
		Case aCase = optionalCase.orElseThrow(() -> new IllegalArgumentException(
				"Case not found with id: " + caseId + ". Please ensure the ID is correct "));

		Argument argument = aCase.getArgument(argumentId);
		model.put("argument", argument);
		model.put("case", aCase);

		Evaluation evaluation = new Evaluation();
		User user = principal.getUser();
		evaluation.setUser(user);

		argument.addEvaluation(evaluation);
		return evaluation;
	}

	@ModelAttribute("intensityTypes")
	public List<IntensityType> populateIntensityTypes() {
		return Arrays.stream(IntensityType.values()).toList();
	}

	@ModelAttribute("verificationTypes")
	public List<VerificationStatus> populateVerificationTypes() {
		return Arrays.stream(VerificationStatus.values()).toList();
	}

	@GetMapping("/cases/{caseId}/arguments/{argumentId}/evaluations/new")
	@PreAuthorize("@caseSecurity.checkCaseAccess(#caseId, authentication)")
	public String initNewEvaluationForm(@PathVariable("caseId") int caseId) {
		return "arguments/createOrUpdateEvaluationsForm";
	}

	// Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is
	// called
	@PostMapping("/cases/{caseId}/arguments/{argumentId}/evaluations/new")
	@PreAuthorize("@caseSecurity.checkCaseAccess(#caseId, authentication)")
	public String processNewEvaluationForm(@ModelAttribute Case aCase, @PathVariable int argumentId,
			@Valid Evaluation evaluation, BindingResult result, RedirectAttributes redirectAttributes,
			@PathVariable("caseId") int caseId) {
		if (result.hasErrors()) {
			return "arguments/createOrUpdateEvaluationsForm";
		}

		aCase.addEvaluation(argumentId, evaluation);
		this.cases.save(aCase);
		redirectAttributes.addFlashAttribute("message", "Added.");
		return "redirect:/cases/{caseId}";
	}

	@GetMapping("/cases/{caseId}/arguments/{argumentId}/evaluations/{evaluationId}/delete")
	@PreAuthorize("@caseSecurity.checkCaseAccess(#caseId, authentication)")
	public String deleteEvaluation(@PathVariable("caseId") int caseId, @PathVariable int argumentId,
			@PathVariable int evaluationId, RedirectAttributes redirectAttributes) {
		Optional<Case> optionalCase = cases.findById(caseId);
		Case aCase = optionalCase
			.orElseThrow(() -> new IllegalArgumentException("Case with id " + caseId + " not found."));
		aCase.deleteEvaluation(argumentId, evaluationId);
		cases.save(aCase);
		redirectAttributes.addFlashAttribute("message", "Evaluation deleted successfully");
		return "redirect:/cases/" + caseId;
	}

}
