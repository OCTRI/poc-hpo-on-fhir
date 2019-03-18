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
import org.monarchinitiative.fhir2hpo.service.HpoService;
import org.monarchinitiative.fhir2hpo.service.ObservationAnalysisService;
import org.octri.hpoonfhir.domain.FhirSessionInfo;
import org.octri.hpoonfhir.service.FhirService;
import org.octri.hpoonfhir.service.PhenotypeSummaryService;
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

@Controller
public class MainController {
	
	@Autowired
	private FhirService fhirService;
	
	@Autowired
	private FhirSessionInfo fhirSessionInfo;

	@Autowired
	private ObservationAnalysisService observationAnalysisService;

	@Autowired
	private PhenotypeSummaryService phenotypeSummaryService;

	@Autowired
	private HpoService hpoService;

	/**
	 * Return to a clean search form with no results.
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@GetMapping("/")
	public String home(Map<String, Object> model, HttpServletRequest request) {
		// Make sure token is present even though we won't use
		fhirSessionInfo.assertToken();
		// Load text describing caveats for the FHIR Service. This is
		// temporary but will help users if we decide to deploy this to a public location.
		String caveats = null;
		if (fhirService.getServiceName().equals("SmartHealth IT")) {
			caveats = "Note that many patients in this sandbox do not have Observations or the Observations do not "
				+ "provide enough information to infer a phenotype. For a good demonstration, try Frank Taylor "
				+ "or Kristyn Walker.";
		} else if (fhirService.getServiceName().equals("Open Epic")) {
			caveats = "Note that Open Epic only has a half dozen patients, and the API enforces both a first and "
				+ "last name be used to search. Try Jason Argonaut, then select the id beginning with Tbt3KuC...";
		}
		
		model.put("fhirServiceName", fhirService.getServiceName());
		model.put("caveats", caveats);
		model.put("patientSearchForm", new PatientModel());
		model.put("results", false);
		return "search";
	}

	/**
	 * Search using the form parameters, and return results.
	 * 
	 * @param model
	 * @param form
	 * @return
	 */
	@PostMapping("/")
	public String search(HttpServletRequest request, Map<String, Object> model, @ModelAttribute PatientModel form) {
		String token = fhirSessionInfo.assertToken();
		model.put("fhirServiceName", fhirService.getServiceName());
		model.put("patientSearchForm", form);
		List<Patient> patients = fhirService.findPatientsByFullName(token, form.getFirstName(), form.getLastName());
		List<PatientModel> patientModels = patients.stream()
				.map(fhirPatient -> new PatientModel(fhirPatient))
				.collect(Collectors.toList());
		model.put("patients", patientModels);
		model.put("results", true);
		return "search";
	}

	/**
	 * Get the patient information and return to the phenotypes view
	 * @param model
	 * @param id the patient id
	 * @return the patient found
	 */
	@GetMapping("/patient/{id:.+}")
	public String patient(HttpServletRequest request, Map<String, Object> model, @PathVariable String id) {
		String token = fhirSessionInfo.assertToken();
		Patient fhirPatient = fhirService.findPatientById(token, id);
		PatientModel patientModel = new PatientModel(fhirPatient);
		model.put("patient", patientModel);
		return "patient";
	}

	/**
	 * Get the patient information and return to the phenotypes view
	 * @param model
	 * @param id the patient id
	 * @return the patient found
	 */
	@GetMapping("/patient/{id:.+}/phenotype")
	public String phenoytype(HttpServletRequest request, Map<String, Object> model, @PathVariable String id) {
		String token = fhirSessionInfo.assertToken();
		Patient fhirPatient = fhirService.findPatientById(token, id);
		PatientModel patientModel = new PatientModel(fhirPatient);

		model.put("patient", patientModel);
		model.put("includeHpoSummaryJs", true);
		return "phenotypes";
		// Epic's browser can't handle js and data tables, so we'll do a raw table for testing
		//List<PhenotypeModel> phenotypes = phenotypeSummaryService.summarizePhenotypes(fhirService.findObservationsForPatient(token, id));
		//model.put("phenotypes", phenotypes);
		//return "phenotypes-epic";
	}

	/**
	 * Get the patient information and return to the phenotypes view
	 * @param model
	 * @param id the patient id
	 * @return the patient found
	 * @throws MismatchedLoincIdException 
	 */
	@GetMapping("/patient/{id:.+}/observation")
	public String observations(HttpServletRequest request, Map<String, Object> model, @PathVariable String id) throws MismatchedLoincIdException {
		String token = fhirSessionInfo.assertToken();
		Patient fhirPatient = fhirService.findPatientById(token, id);
		PatientModel patientModel = new PatientModel(fhirPatient);
		List<Observation> observations = fhirService.findObservationsForPatient(token, id);
		List<ObservationPhenotypeModel> observationModels = new ArrayList<>();
		List<ObservationConversionResult> conversionResults = new ArrayList<>();
		for (Observation o : observations) {
			ObservationConversionResult conversionResult =  observationAnalysisService.analyzeObservation(o);
			conversionResults.add(conversionResult);
			Set<LoincId> loincs = ObservationUtil.getAllLoincIdsOfObservation(o);
			for (LoincId loinc : loincs) {
				LoincConversionResult result = conversionResult.getLoincConversionResults().stream().filter(res -> res.getLoincId().equals(loinc))
					.findFirst().get();
				String termNames = "No methods successful";
				if (result.hasSuccess()) {
					Set<HpoTermWithNegation> terms = result.getHpoTerms();
					termNames = terms.stream().map(term -> (term.isNegated() ? "NOT " : "") + hpoService.getTermForTermId(term.getHpoTermId()).getName()).collect(Collectors.joining(","));
				} else if (result.hasException()) {
					termNames = result.getException().getMessage();
				}
				
				ObservationPhenotypeModel observationModel = new ObservationPhenotypeModel(loinc.getCode(), new ObservationLoincInfo(loinc, o), termNames);
				observationModels.add(observationModel);
			}
		}
		
		Collections.sort(observationModels);
		model.put("patient", patientModel);
		model.put("includeObservationSummaryJs", true);
		model.put("observations", observationModels);
		model.put("summary", new SummaryStatsModel(conversionResults));
		return "observation/list";
	}

	/**
	 * Show the observation
	 * @param request
	 * @param model
	 * @param patient
	 * @param observation
	 * @return
	 */
	@GetMapping("/patient/{patient:.+}/observation/{observation:.+}")
	public String observation(HttpServletRequest request, Map<String, Object> model, @PathVariable String patient, @PathVariable String observation) {
		String param = request.getParameter("phenotypeList");
		String token = fhirSessionInfo.assertToken();
		Patient fhirPatient = fhirService.findPatientById(token, patient);
		PatientModel patientModel = new PatientModel(fhirPatient);
		Observation o = fhirService.findObservationById(token, observation);
		String json = FhirContext.forDstu3().newJsonParser().setPrettyPrint(true).encodeResourceToString(o);
		List<LoincConversionResult> loincConversionResults = observationAnalysisService.analyzeObservation(o).getLoincConversionResults();
		loincConversionResults.get(0).hasException();
		model.put("patient", patientModel);
		model.put("observation", observation);
		model.put("json", json);
		model.put("results", loincConversionResults);
		model.put("phenotypeList", param);
		return "observation/show";
	}
	
}
