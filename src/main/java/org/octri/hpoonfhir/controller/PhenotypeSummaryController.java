package org.octri.hpoonfhir.controller;

import java.util.List;
import java.util.Map;

import org.hl7.fhir.exceptions.FHIRException;
import org.octri.hpoonfhir.service.AuthenticationService;
import org.octri.hpoonfhir.service.FhirService;
import org.octri.hpoonfhir.service.PhenotypeSummaryService;
import org.octri.hpoonfhir.view.PhenotypeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
	
	@Autowired 
	AuthenticationService authenticationService;

	/**
	 * Get the patient observations and convert them to HPO if possible.
	 * @param model
	 * @param request
	 * @return a JSON string representing the summary of phenotypes found
	 */
	@GetMapping("/summary/{id:.+}")
	public String labs(Map<String, Object> model, OAuth2AuthenticationToken authentication, @PathVariable String id) {
		String json = "";
		try {
			String token = authenticationService.getTokenString(authentication);
			List<PhenotypeModel> phenotypes = phenotypeSummaryService.summarizePhenotypes(fhirService.findObservationsForPatient(token, id));
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


}
