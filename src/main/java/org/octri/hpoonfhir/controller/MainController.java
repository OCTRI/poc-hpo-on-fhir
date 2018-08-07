package org.octri.hpoonfhir.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.exceptions.FHIRException;
import org.monarchinitiative.fhir2hpo.loinc.DefaultLoinc2HpoAnnotation;
import org.monarchinitiative.fhir2hpo.loinc.Loinc2HpoAnnotation;
import org.monarchinitiative.fhir2hpo.loinc.LoincId;
import org.monarchinitiative.fhir2hpo.service.AnnotationService;
import org.octri.hpoonfhir.service.FhirService;
import org.octri.hpoonfhir.service.PhenotypeSummaryService;
import org.octri.hpoonfhir.view.PatientModel;
import org.octri.hpoonfhir.view.PhenotypeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {

	@Autowired
	AnnotationService annotationService;

	@Autowired
	@Qualifier("r3FhirService")
	FhirService fhirService;

	@Autowired
	PhenotypeSummaryService phenotypeSummaryService;

	/**
	 * Return to a clean search form with no results.
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@GetMapping("/")
	public String home(Map<String, Object> model, HttpServletRequest request) {

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
	public String search(Map<String, Object> model, @ModelAttribute PatientModel form) {
		model.put("patientSearchForm", form);
		try {
			List<Patient> patients = fhirService.findPatientsByFullName(form.getFirstName(), form.getLastName());
			List<PatientModel> patientModels = patients.stream()
					.map(fhirPatient -> new PatientModel(fhirPatient))
					.collect(Collectors.toList());
			model.put("patients", patientModels);
			model.put("results", true);
		} catch (FHIRException e) {
			// TODO: Decide how to handle FHIRException which is only thrown if there's an error in the conversion of V2
			// messages
			e.printStackTrace();
		}
		return "search";
	}

	/**
	 * Get the patient observations and convert them to HPO if possible.
	 * @param model
	 * @param request
	 * @return the patient and the phenotypes found
	 */
	@GetMapping("/phenotypes/{id}")
	public String labs(Map<String, Object> model, @PathVariable String id) {
		try {
			// Get the patient again so information can be displayed
			Patient fhirPatient = fhirService.findPatientById(id);
			PatientModel patientModel = new PatientModel(fhirPatient);
			model.put("patient", patientModel);
			List<Observation> observations = fhirService.findObservationsForPatient(id);
			List<PhenotypeModel> phenotypes = phenotypeSummaryService.summarizePhenotypes(observations);

			model.put("phenotypes", phenotypes);
		} catch (FHIRException e) {
			e.printStackTrace();
		}
		return "phenotypes";
	}

	@RequestMapping("/annotations")
	public String annotations(Map<String, Object> model) throws Exception {
		Map<LoincId, Loinc2HpoAnnotation> annotations = annotationService.getAnnotationsMap();
		List<LoincId> loincs = annotations.entrySet().stream()
				.filter(x -> x.getValue() instanceof DefaultLoinc2HpoAnnotation).map(x -> x.getKey())
				.collect(Collectors.toList());
		model.put("loincs", loincs);

		return "annotations";
	}

	@RequestMapping("/hpo")
	public String hpo(Map<String, Object> model, HttpServletRequest request) throws Exception {
		String loincId = request.getParameter("loinc_id");
		Loinc2HpoAnnotation annotation = annotationService.getAnnotations(new LoincId(loincId));
		model.put("annotation", annotation.toString());

		return "hpo";
	}

}
