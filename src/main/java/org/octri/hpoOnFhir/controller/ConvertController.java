package org.octri.hpoOnFhir.controller;

import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Observation;
import org.monarchinitiative.fhir2hpo.hpo.HpoTermWithNegation;
import org.octri.hpoOnFhir.service.ObservationAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

@RestController
public class ConvertController {
	
	@Autowired
	ObservationAnalysisService observationAnalysisService;
	private IParser jsonParser;
	
	public ConvertController() {
		jsonParser = FhirContext.forDstu3().newJsonParser();	
	}
	
	@PostMapping("/convert")
	public Map<String, Object> convert(String resource) throws Exception {
		Map<String, Object> out = new HashMap<>();	
		
		Observation observation = (Observation) jsonParser.parseResource(resource);
		HpoTermWithNegation term = observationAnalysisService.analyzeObservation(observation);
		out.put("hpoTerm", term == null ? "Not Found" : term.getHpoTerm().getName() + ":" + term.isNegated());
		return out;
	}

}
