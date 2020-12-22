package org.octri.hpoonfhir.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.model.Observation;
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
			List<PhenotypeModel> phenotypes = phenotypeSummaryService.summarizePhenotypes(fhirService.findObservationsForPatient(id, null));
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

		Observation hpoObservation = PhenotypeSummaryService.buildPhenotypeObservation(patientId, hpoTermId, hpoTermName, observations,
				comments);        
        fhirService.createUpdateObservation(hpoObservation);

		return "ok";
	}

	@PostMapping("/deleteHpo")
	public String deleteHpo(Map<String, Object> model, @RequestParam String patientId, 
			@RequestParam String hpoTermId) {
		List<Observation> hpoObservations = fhirService.findObservationsForPatient(patientId, PhenotypeSummaryService.PHENOPACKETS_OBSERVATION_CATEGORY);
		List<Observation> observationsForTermId = hpoObservations.stream().filter(o -> o.getCode().getCode(PhenotypeSummaryService.PHENOPACKETS_URL).equals(hpoTermId)).collect(Collectors.toList());
		for (Observation o : observationsForTermId) {
			fhirService.deleteResourceById(o.getIdElement());
		}
		return "ok";
	}

	

}
