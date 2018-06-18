package org.octri.hpoonfhir.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Observation;
import org.monarchinitiative.fhir2hpo.hpo.HpoTermWithNegation;
import org.octri.hpoonfhir.service.ObservationAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

@RestController
public class ConvertController {
	
	@Autowired
	ObservationAnalysisService observationAnalysisService;
	private final IParser jsonParser;
	
	public ConvertController() {
		jsonParser = FhirContext.forDstu3().newJsonParser();	
	}
	
	@PostMapping("/convert")
	public Map<String, Object> convert(String resource) throws Exception {
		Map<String, Object> out = new HashMap<>();	
		
		Observation observation = (Observation) jsonParser.parseResource(resource);
		Set<HpoTermWithNegation> terms = observationAnalysisService.analyzeObservation(observation);
		String result = terms.size() == 0 ? "Not Found": terms.stream().map(term -> term.toString()).collect(Collectors.joining(","));
		out.put("hpoTerm", result);
		return out;
	}

}
