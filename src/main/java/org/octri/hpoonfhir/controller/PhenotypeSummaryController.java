package org.octri.hpoonfhir.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.model.Annotation;
import org.hl7.fhir.r5.model.BooleanType;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.Observation;
import org.hl7.fhir.r5.model.Patient;
import org.hl7.fhir.r5.model.Reference;
import org.octri.hpoonfhir.service.FhirService;
import org.octri.hpoonfhir.service.PhenotypeSummaryService;
import org.octri.hpoonfhir.view.PhenotypeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This REST controller handles the AJAX request to summarize the phenotypes for a patient.
 * 
 * @author yateam
 *
 */
@RestController
public class PhenotypeSummaryController {

	@Autowired
	FhirService fhirService;

	@Autowired
	PhenotypeSummaryService phenotypeSummaryService;

	/**
	 * Get the patient observations and convert them to HPO if possible.
	 * @param model
	 * @param request
	 * @return a JSON string representing the summary of phenotypes found
	 */
	@GetMapping("/summary/{id:.+}")
	public String labs(Map<String, Object> model, @PathVariable String id) {
		String json = "";
		try {
			List<PhenotypeModel> phenotypes = phenotypeSummaryService.summarizePhenotypes(fhirService.findObservationsForPatient(id));
			model.put("data", phenotypes);
			ObjectMapper objectMapper = new ObjectMapper();
			json = objectMapper.writeValueAsString(model);
		} catch (FHIRException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} 
		return json;
	}

	@PostMapping("/reportHpo")
	public String reportHpo(Map<String, Object> model, @RequestParam String patientId, 
			@RequestParam String hpoTermId, @RequestParam String hpoTermName, 
			@RequestParam String observations, @RequestParam String comments) {

		Observation hpoObservation = new Observation();
		hpoObservation.setId(String.valueOf(new Random().nextLong()));
		
		Identifier identifier = new Identifier().setSystem("http://hpo.jax.org").setValue(patientId + "|" + hpoTermId);
		hpoObservation.getIdentifier().add(identifier);
		
		hpoObservation
			.getCode()
			.setText(hpoTermName)
			.addCoding()
			.setSystem("http://github.com/phenopackets/core-ig/CodeSystem/hpo")
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

		return "ok";
	}

	@PostMapping("/deleteHpo")
	public String deleteHpo(Map<String, Object> model, @RequestParam String patientId, 
			@RequestParam String hpoTermId) {
		List<Observation> hpoObservations = fhirService.findObservationsForPatientAndCategory(patientId, "hpo");
		List<Observation> observationsForTermId = hpoObservations.stream().filter(o -> o.getCode().getCode("http://hpo.jax.org").equals(hpoTermId)).collect(Collectors.toList());
		for (Observation o : observationsForTermId) {
			fhirService.deleteResourceById(o.getIdElement());
		}
		return "ok";
	}

	

}
