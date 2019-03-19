package org.octri.hpoonfhir.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.monarchinitiative.fhir2hpo.fhir.util.ObservationLoincInfo;
import org.monarchinitiative.fhir2hpo.fhir.util.ObservationUtil;
import org.monarchinitiative.fhir2hpo.hpo.HpoTermWithNegation;
import org.monarchinitiative.fhir2hpo.hpo.LoincConversionResult;
import org.monarchinitiative.fhir2hpo.hpo.ObservationConversionResult;
import org.monarchinitiative.fhir2hpo.loinc.LoincId;
import org.monarchinitiative.fhir2hpo.loinc.exception.MismatchedLoincIdException;
import org.octri.hpoonfhir.domain.FhirSessionInfo;
import org.octri.hpoonfhir.service.FhirService;
import org.octri.hpoonfhir.view.ConditionModel;
import org.octri.hpoonfhir.view.ObservationPhenotypeModel;
import org.octri.hpoonfhir.view.PatientModel;
import org.octri.hpoonfhir.view.SummaryStatsModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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

	/**
	 * List the patient's conditions
	 * @param model
	 * @param id the patient id
	 * @return the patient found
	 * @throws MismatchedLoincIdException 
	 */
	@GetMapping("/patient/{id:.+}/condition")
	public String conditions(HttpServletRequest request, Map<String, Object> model, @PathVariable String id) throws MismatchedLoincIdException {
		String token = fhirSessionInfo.assertToken();
		Patient fhirPatient = fhirService.findPatientById(token, id);
		PatientModel patientModel = new PatientModel(fhirPatient);
		List<Condition> conditions = fhirService.findConditionsForPatient(token, id);
		IParser parser = FhirContext.forDstu3().newJsonParser().setPrettyPrint(true);
		List<ConditionModel> conditionModels = conditions.stream().map(condition -> new ConditionModel(condition, parser)).collect(Collectors.toList());
		
		Collections.sort(conditionModels);
		model.put("patient", patientModel);
		model.put("includeSummaryJs", true);
		model.put("conditions", conditionModels);
		return "condition/list";
	}

	/**
	 * Show the condition
	 * @param request
	 * @param model
	 * @param patient id
	 * @param condition id
	 * @return
	 */
	@GetMapping("/patient/{patient:.+}/condition/{condition:.+}")
	public String condition(HttpServletRequest request, Map<String, Object> model, @PathVariable String patient, @PathVariable String condition) {
		String token = fhirSessionInfo.assertToken();
		Patient fhirPatient = fhirService.findPatientById(token, patient);
		PatientModel patientModel = new PatientModel(fhirPatient);
		Condition c = fhirService.findConditionById(token, condition);
		String json = FhirContext.forDstu3().newJsonParser().setPrettyPrint(true).encodeResourceToString(c);
		model.put("patient", patientModel);
		model.put("condition", condition);
		model.put("json", json);
		return "condition/show";
	}

}
