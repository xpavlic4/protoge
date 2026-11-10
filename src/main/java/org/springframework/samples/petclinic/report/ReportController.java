/*
 * Copyright 2012-2025 the original author or authors.
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
package org.springframework.samples.petclinic.report;

import org.springframework.samples.petclinic.cases.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
class ReportController {

	private final CaseRepository cases;

	private final ArgumentRepository argumentRepository;

	ReportController(CaseRepository cases, ArgumentRepository argumentRepository) {
		this.cases = cases;
		this.argumentRepository = argumentRepository;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	public Case findCase(Integer caseId) {
		return caseId == null ? new Case()
				: this.cases.findById(caseId)
					.orElseThrow(() -> new IllegalArgumentException("Case not found with id: " + caseId
							+ ". Please ensure the ID is correct " + "and the owner exists in the database."));
	}

	@ModelAttribute("reportOverruled")
	public List<CaseReport> generateReportOverruled(@PathVariable(name = "caseId") Integer caseId) {
		Case aCase = findCase(caseId);
		List<CaseReport> ret = new ArrayList<>();
		List<Argument> allArguments = argumentRepository.findByACaseId(aCase.getId());
		for (Argument argument : allArguments) {
			for (Evaluation evaluation : argument.getEvaluations()) {
				VerificationStatus status = evaluation.getVerification_status();
				if (status.equals(VerificationStatus.OVERRULED) || status.equals(VerificationStatus.DEFENSIBLE)) {
					CaseReport report = new CaseReport();
					report.setPremise(argument.getPremise());
					report.setPredicate(argument.getPredicate());
					report.setVerificationStatus(status);
					ret.add(report);
					break;
				}
			}

		}
		return ret;
	}

	@ModelAttribute("report")
	public List<CaseReport> generateReport(@PathVariable(name = "caseId") Integer caseId) {
		Case aCase = findCase(caseId);
		List<CaseReport> ret = new ArrayList<>();
		// List<Argument> rootAruments =
		// argumentRepository.findRootAruments(aCase.getId());
		List<Argument> allArguments = argumentRepository.findByACaseId(aCase.getId());
		for (Argument argument : allArguments) {
			for (Evaluation evaluation : argument.getEvaluations()) {
				VerificationStatus status = evaluation.getVerification_status();
				if (status.equals(VerificationStatus.JUSTIFIED)) {
					CaseReport report = new CaseReport();
					report.setPremise(argument.getPremise());
					report.setPredicate(argument.getPredicate());
					report.setVerificationStatus(status);
					ret.add(report);
					break;
				}
			}
		}
		return ret;

	}

	/**
	 * Custom handler for displaying a report.
	 * @param caseId the ID of the owner to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/reports/{caseId}")
	@PreAuthorize("@caseSecurity.checkCaseOwner(#caseId, authentication)")
	public ModelAndView showOwner(@PathVariable("caseId") int caseId) {
		ModelAndView mav = new ModelAndView("reports/reportDetails");
		Optional<Case> optionalOwner = this.cases.findById(caseId);
		Case owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Case not found with id: " + caseId + ". Please ensure the ID is correct "));
		mav.addObject(owner);
		return mav;
	}

}
