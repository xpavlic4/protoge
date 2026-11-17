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

import java.io.IOException;
import java.util.*;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.export.ArgumentView;
import org.springframework.samples.petclinic.export.ExcelExportService;
import org.springframework.samples.petclinic.export.TreeDiagramService;
import org.springframework.samples.petclinic.system.CustomUserPrincipal;
import org.springframework.samples.petclinic.system.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
class CaseController {

	private static final String VIEWS_CASE_CREATE_OR_UPDATE_FORM = "cases/createOrUpdateCaseForm";

	private final CaseRepository cases;

	private final ExcelExportService excelExportService;

	private final ArgumentRepository argumentRepository;

	private final TreeDiagramService treeService;

	private final CaseRoleService caseRoleService;

	private final CaseRoleRepository caseRoleRepository;

	public CaseController(CaseRepository clinicService, ExcelExportService excelExportService,
			ArgumentRepository argumentRepository, TreeDiagramService treeService, CaseRoleService caseRoleService,
			CaseRoleRepository caseRoleRepository) {
		this.caseRoleService = caseRoleService;
		this.caseRoleRepository = caseRoleRepository;
		this.cases = clinicService;
		this.excelExportService = excelExportService;
		this.argumentRepository = argumentRepository;
		this.treeService = treeService;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@ModelAttribute("case")
	public Case findOwner(@PathVariable(name = "caseId", required = false) Integer caseId,
			@AuthenticationPrincipal CustomUserPrincipal principal) {
		if (caseId == null) {
			Case aCase = new Case();
			aCase.setUser(principal.getUser());
			return aCase;
		}
		Case aCase = this.cases.findById(caseId)
			.orElseThrow(() -> new IllegalArgumentException("Case not found with id: " + caseId
					+ ". Please ensure the ID is correct " + "and the case exists in the database."));
		aCase.setPlaintiffEmail(caseRoleService.emailOf(caseId, CaseRole.PLAINTIFF));
		aCase.setDefendantEmail(caseRoleService.emailOf(caseId, CaseRole.DEFENDANT));
		return aCase;
	}

	private boolean rejectInvalidParties(Case aCase, User judge, BindingResult result) {
		caseRoleService.validateParties(judge, aCase.getPlaintiffEmail(), aCase.getDefendantEmail())
			.forEach((field, code) -> result.rejectValue(field, code, code));
		return result.hasErrors();
	}

	@GetMapping("/cases/new")
	public String initCreationForm() {
		return VIEWS_CASE_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/cases/new")
	public String processCreationForm(@Valid Case aCase, BindingResult result, RedirectAttributes redirectAttributes,
			@AuthenticationPrincipal CustomUserPrincipal principal) {
		if (!result.hasErrors()) {
			rejectInvalidParties(aCase, principal.getUser(), result);
		}
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in creating the case.");
			return VIEWS_CASE_CREATE_OR_UPDATE_FORM;
		}
		aCase.setUser(principal.getUser());
		this.cases.save(aCase);
		caseRoleService.assignInitial(aCase, principal.getUser(), aCase.getPlaintiffEmail(), aCase.getDefendantEmail());
		redirectAttributes.addFlashAttribute("message", "Created.");
		return "redirect:/cases/" + aCase.getId();
	}

	@GetMapping("/cases/{caseId}/export")
	@PreAuthorize("@caseSecurity.checkCaseAccess(#caseId, authentication)")
	public void exportExcel(HttpServletResponse response, @Valid Case aCase, @PathVariable("caseId") int caseId)
			throws IOException {
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition", "attachment; filename=case-" + aCase.getId() + ".xlsx");

		List<List<String>> data = new ArrayList<>();
		data.add(List.of("ID", "Argument", "Predicate", "inference", "Premise"));
		for (Argument rootArument : argumentRepository.findRootAruments(aCase.getId())) {
			List<String> map = new ArrayList<>();
			map.add(rootArument.getId().toString());
			map.add("argument");
			map.add(rootArument.getPredicate());
			map.add(" ,protože/because");
			map.add(rootArument.getPremise());
			// data.add(TreeMap.("ID", "", "Argument", "A1", "predikát (P)", "P1: Chodec
			// měl vhodnou obuv", "inference",
			// " ,protože/because", "premisa (Z)", "Z1: podrážka boty měla hrubý vzorek.
			// "));
			data.add(map);
			Set<Argument> attacks = rootArument.getAttacks();
			for (Argument attack : attacks) {
				map = new LinkedList<>();
				map.add(attack.getId().toString());
				map.add("attack");
				map.add(attack.getPredicate());
				map.add(" ,protože/because");
				map.add(attack.getPremise());
				data.add(map);
			}
		}
		// List<Map<String, Object>> data = List
		// .of(Map.of("ID", "p.A1.P1.Z1", "Argument", "A1", "predikát (P)", "P1: Chodec
		// měl vhodnou obuv", "inference",
		// " ,protože/because", "premisa (Z)", "Z1: podrážka boty měla hrubý vzorek. "));

		byte[] excelData = excelExportService.generateExcel(data);
		response.getOutputStream().write(excelData);
	}

	@GetMapping("/cases/{caseId}/tree")
	@PreAuthorize("@caseSecurity.checkCaseAccess(#caseId, authentication)")
	public String viewDashboard(Model model, @Valid Case aCase, @PathVariable("caseId") int caseId) {
		ArgumentView treeData = treeService.generateTreeForCase(aCase.getId());
		model.addAttribute("tree", treeData);
		return "reports/tree";
	}

	@GetMapping("/cases/find")
	public String initFindForm() {
		return "cases/findCases";
	}

	@PreAuthorize("@caseSecurity.checkCaseOwner(#caseId, authentication)")
	@GetMapping("/cases/{caseId}/edit")
	public String initUpdateOwnerForm(@PathVariable("caseId") int caseId) {
		return VIEWS_CASE_CREATE_OR_UPDATE_FORM;
	}

	@PreAuthorize("@caseSecurity.checkCaseOwner(#caseId, authentication)")
	@PostMapping("/cases/{caseId}/edit")
	public String processUpdateOwnerForm(@Valid Case aCase, BindingResult result, @PathVariable("caseId") int caseId,
			RedirectAttributes redirectAttributes, @AuthenticationPrincipal CustomUserPrincipal principal) {
		if (!result.hasErrors()) {
			rejectInvalidParties(aCase, aCase.getUser(), result);
		}
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in updating the case.");
			return VIEWS_CASE_CREATE_OR_UPDATE_FORM;
		}

		if (aCase.getId() != caseId) {
			result.rejectValue("id", "mismatch", "The case ID in the form does not match the URL.");
			redirectAttributes.addFlashAttribute("error", "Owner ID mismatch. Please try again.");
			return "redirect:/cases/{caseId}/edit";
		}

		aCase.setId(caseId);
		// keep the original creator: the form does not carry the owner
		aCase.setUser(this.cases.findById(caseId).orElseThrow().getUser());
		this.cases.save(aCase);
		caseRoleService.updateParties(aCase, aCase.getPlaintiffEmail(), aCase.getDefendantEmail());
		redirectAttributes.addFlashAttribute("message", "Case Values Updated");
		return "redirect:/cases/{caseId}";
	}

	/**
	 * Custom handler for displaying an owner.
	 * @param caseId the ID of the owner to display
	 * @return a ModelMap with the model attributes for the view
	 */
	// Dynamically passes the ID and Google Principal email to your custom bean
	@PreAuthorize("@caseSecurity.checkCaseAccess(#caseId, authentication)")
	@GetMapping("/cases/{caseId}")
	public ModelAndView showCase(@PathVariable("caseId") int caseId) {
		ModelAndView mav = new ModelAndView("cases/caseDetails");
		Optional<Case> optionalCase = this.cases.findById(caseId);
		Case aCase = optionalCase.orElseThrow(() -> new IllegalArgumentException(
				"Case not found with id: " + caseId + ". Please ensure the ID is correct "));
		mav.addObject(aCase);
		List<Argument> rootAruments = argumentRepository.findRootAruments(caseId);
		rootAruments.sort(Comparator.comparingInt(a -> a.getEvaluations().size()));
		mav.addObject("rootAruments", rootAruments);
		mav.addObject("caseRoles", caseRoleRepository.findByCaseId(caseId));

		return mav;
	}

	/**
	 * Cases
	 */

	@GetMapping("/cases")
	public String processFindForm(@RequestParam(defaultValue = "1") int page, Case aCase, BindingResult result,
			Model model, @AuthenticationPrincipal CustomUserPrincipal principal) {
		// allow parameterless GET request for /owners to return all records
		if (aCase.getName() == null) {
			aCase.setName(""); // empty string signifies broadest possible search
		}

		// find owners by last name
		Page<Case> casesResult = findPaginatedForCasesLastName(page, aCase.getName(), principal.getUser().getId());
		if (casesResult.isEmpty()) {
			// no owners found
			result.rejectValue("name", "notFound", "not found");
			return "cases/findCases";
		}

		if (casesResult.getTotalElements() == 1) {
			// 1 case found
			aCase = casesResult.iterator().next();
			return "redirect:/cases/" + aCase.getId();
		}

		// multiple owners found
		return addPaginationModelCases(page, model, casesResult);
	}

	private String addPaginationModelCases(int page, Model model, Page<Case> paginated) {
		List<Case> listOwners = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listCases", listOwners);
		return "cases/casesList";
	}

	private Page<Case> findPaginatedForCasesLastName(int page, String lastname, Long userId) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		// return cases.findByNameStartingWith(lastname, pageable);
		return cases.findByNameStartingWithAndMember(lastname, userId, pageable);
	}

}
