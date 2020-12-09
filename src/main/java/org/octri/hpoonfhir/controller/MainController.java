package org.octri.hpoonfhir.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.model.Annotation;
import org.hl7.fhir.r5.model.BooleanType;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.Observation;
import org.hl7.fhir.r5.model.Patient;
import org.hl7.fhir.r5.model.Reference;
import org.monarchinitiative.fhir2hpo.loinc.DefaultLoinc2HpoAnnotation;
import org.monarchinitiative.fhir2hpo.loinc.Loinc2HpoAnnotation;
import org.monarchinitiative.fhir2hpo.loinc.LoincId;
import org.monarchinitiative.fhir2hpo.service.AnnotationService;
import org.octri.hpoonfhir.service.FhirService;
import org.octri.hpoonfhir.view.PatientModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {
	
	@Autowired
	AnnotationService annotationService;

	@Autowired
	FhirService fhirService;

	/**
	 * Display a hardcoded list of patients that can be browsed
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@GetMapping("/")
	public String home(Map<String, Object> model, HttpServletRequest request) {

		List<PatientModel> patientModels = new ArrayList<>();
		
		try {
			List<Patient> fhirPatients = fhirService.findPatientsByFullName("John", "McLean");
			if (fhirPatients.size() > 1) {
				patientModels.add(new PatientModel(fhirPatients.get(0)));
			}
			//patientModels.add(new PatientModel(fhirService.findPatientById("1505")));
			//patientModels.add(new PatientModel(fhirService.findPatientById("1652")));
			//patientModels.add(new PatientModel(fhirService.findPatientById("863165")));
			//patientModels.add(new PatientModel(fhirService.findPatientById("JHU473711")));
		} catch (FHIRException e) {
			// Do nothing. Assume something's wrong with the sandbox and display a message indicating this.
			e.printStackTrace();
		}
		
		model.put("fhirServiceName", fhirService.getServiceName());
		model.put("patients", patientModels);
		return "browse";
	}

	/**
	 * Get the patient information and return to the phenotypes view
	 * @param model
	 * @param id the patient id
	 * @return the patient found
	 */
	@GetMapping("/patient/{id:.+}")
	public String patient(Map<String, Object> model, @PathVariable String id) {
		try {
			// Get the patient again so information can be displayed
			Patient fhirPatient = fhirService.findPatientById(id);
			PatientModel patientModel = new PatientModel(fhirPatient);
			model.put("patient", patientModel);
			model.put("includeHpoSummaryJs", true);
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
	
	@PostMapping("/reportHpo")
	public String reportHpo(Map<String, Object> model, @RequestParam String patientId, 
			@RequestParam String hpoTermId, @RequestParam String hpoTermName, 
			@RequestParam String observations, @RequestParam String comments) {

		Observation hpoObservation = new Observation();
		hpoObservation.setId(String.valueOf(new Random().nextLong()));
		// TODO: Temp
		hpoObservation.setId("1673");
		
		Identifier identifier = new Identifier().setSystem("http://hpo.jax.org").setValue(patientId + "|" + hpoTermId);
		hpoObservation.getIdentifier().add(identifier);
		
		hpoObservation
			.getCode()
			.addCoding()
			.setSystem("http://hpo.jax.org")
			.setCode(hpoTermId)
			.setDisplay(hpoTermName);
        BooleanType btype = new BooleanType(true);
        hpoObservation.setValue(btype);
        
        // Get the patient from the FHIR server
        Patient patient = fhirService.findPatientById(patientId);
        Reference patientReference = new Reference(patient);
        hpoObservation.setSubject(patientReference);

        // Parse the observation ids, and get the corresponding observations from FHIR server
		List<String> observationIds = Arrays.asList(observations.split(","));
		for (String observationId : observationIds) {
			Observation origObservation = fhirService.findObservationById(observationId);
			Reference reference = new Reference(origObservation);
			hpoObservation.getDerivedFrom().add(reference);
		}

        // Add the comment as a note
		Annotation annotation = new Annotation();
        annotation.setText(comments);
        hpoObservation.getNote().add(annotation);
        
        // Add a custom category - may be able to query with this
        CodeableConcept category = new CodeableConcept();
        category.addCoding("http://hpo.jax.org", "hpo", "HPO");
        hpoObservation.getCategory().add(category);
        
        fhirService.createUpdateObservation(hpoObservation);

		return "redirect:/patient/" + patientId;
	}


	
}
