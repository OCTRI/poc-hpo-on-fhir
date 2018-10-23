package org.octri.hpoonfhir.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.exceptions.FHIRException;
import org.octri.hpoonfhir.service.FhirService;
import org.octri.hpoonfhir.service.FhirSessionService;
import org.octri.hpoonfhir.service.PhenotypeSummaryService;
import org.octri.hpoonfhir.view.PhenotypeModel;
import org.springframework.beans.factory.annotation.Autowired;
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

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	FhirService fhirService;

	@Autowired
	FhirSessionService fhirSessionService;

	@Autowired
	PhenotypeSummaryService phenotypeSummaryService;

	/**
	 * Get the patient observations and convert them to HPO if possible.
	 * @param model
	 * @param request
	 * @return a JSON string representing the summary of phenotypes found
	 */
	@GetMapping("/summary/{id:.+}")
	public String labs(HttpServletRequest request, Map<String, Object> model, @PathVariable String id) {
		String json = "";
		try {
			String token = fhirSessionService.getSessionToken(request);
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
