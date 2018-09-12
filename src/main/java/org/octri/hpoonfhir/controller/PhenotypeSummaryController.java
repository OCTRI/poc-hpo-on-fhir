package org.octri.hpoonfhir.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.hl7.fhir.exceptions.FHIRException;
import org.hspconsortium.client.auth.credentials.ClientSecretCredentials;
import org.hspconsortium.client.session.Session;
import org.hspconsortium.client.session.authorizationcode.AuthorizationCodeSessionFactory;
import org.octri.hpoonfhir.service.FhirService;
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

	@Autowired
    AuthorizationCodeSessionFactory<ClientSecretCredentials> ehrSessionFactory;

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
	public String labs(HttpSession httpSession, Map<String, Object> model, @PathVariable String id) {
        // retrieve the EHR session from the http session
		Session ehrSession = (Session) httpSession.getAttribute(ehrSessionFactory.getSessionKey());
		String json = "";
		try {
			List<PhenotypeModel> phenotypes = phenotypeSummaryService.summarizePhenotypes(fhirService.findObservationsForPatient(ehrSession, id));
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
