package org.octri.hpoOnFhir.controller;

import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Observation;
import org.monarchinitiative.loinc2hpo.fhir.FhirResourceParserDstu3;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.octri.hpoOnFhir.service.ObservationAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConvertController {
	
	@Autowired
	ObservationAnalysisService observationAnalysisService;
	
	@PostMapping("/convert")
	public Map<String, Object> convert(String resource) throws Exception {
		Map<String, Object> out = new HashMap<>();	
		FhirResourceParserDstu3 parser = new FhirResourceParserDstu3();
		Observation observation = (Observation) parser.parse(resource);
		Term term = observationAnalysisService.analyzeObservationThreadsafe(observation);
		out.put("hpoTerm", term == null ? "Not Found" : term.getName());
		System.out.println(out.get("hpoTerm"));
		return out;
	}

}
