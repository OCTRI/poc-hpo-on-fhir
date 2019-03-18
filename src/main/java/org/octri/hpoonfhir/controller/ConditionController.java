package org.octri.hpoonfhir.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.dstu3.model.Condition;
import org.octri.hpoonfhir.domain.FhirSessionInfo;
import org.octri.hpoonfhir.service.FhirService;
import org.octri.hpoonfhir.view.ConditionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

@Controller
public class ConditionController {

	@Autowired
	private FhirService fhirService;
	
	@Autowired
	private FhirSessionInfo fhirSessionInfo;

	/**
	 * Return to a clean search form with no results.
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@GetMapping("/condition/search")
	public String conditionSearch(Map<String, Object> model, HttpServletRequest request) {
		// Make sure token is present even though we won't use
		fhirSessionInfo.assertToken();

		model.put("fhirServiceName", fhirService.getServiceName());
		model.put("conditionSearchForm", new ConditionModel());
		model.put("results", false);
		return "condition/search";
	}

	/**
	 * Search using the form parameters, and return results.
	 * 
	 * @param model
	 * @param form
	 * @return
	 */
	@PostMapping("condition/search")
	public String conditionSearch(HttpServletRequest request, Map<String, Object> model, @ModelAttribute ConditionModel form) {
		String token = fhirSessionInfo.assertToken();
		model.put("fhirServiceName", fhirService.getServiceName());
		model.put("conditionSearchForm", form);
		List<Condition> conditions = fhirService.findConditionsByCode(token, form.getCode());
		IParser parser = FhirContext.forDstu3().newJsonParser().setPrettyPrint(true);
		List<ConditionModel> conditionModels = conditions.stream()
				.map(fhirCondition -> new ConditionModel(fhirCondition, parser))
				.collect(Collectors.toList());
		model.put("conditions", conditionModels);
		model.put("results", true);
		return "condition/search";
	}

}
