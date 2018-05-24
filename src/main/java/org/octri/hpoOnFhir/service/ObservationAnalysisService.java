package org.octri.hpoonfhir.service;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Observation;
import org.monarchinitiative.fhir2hpo.hpo.HpoTermWithNegation;
import org.monarchinitiative.fhir2hpo.loinc.Loinc2HpoAnnotation;
import org.monarchinitiative.fhir2hpo.loinc.LoincId;
import org.monarchinitiative.fhir2hpo.loinc.exception.MalformedLoincCodeException;
import org.monarchinitiative.fhir2hpo.service.AnnotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ObservationAnalysisService {

	@Autowired
	AnnotationService annotationService;

	public HpoTermWithNegation analyzeObservation(Observation observation) {
		try {
			LoincId loincId = getLoincIdOfObservation(observation);
			if (loincId != null) {
				Loinc2HpoAnnotation annotation= annotationService.getAnnotations(loincId);
				if (annotation != null) {
					return annotation.convert(observation);
				}
			}
			
		} catch (Exception e) {
			// Some exception occurred (e.g. term was not found)
			e.printStackTrace();
		}
        
        return null;
	}

	private static LoincId getLoincIdOfObservation(Observation observation) throws MalformedLoincCodeException {

		// TODO: Should this exit out after finding first loinc id or return a list?
		for (Coding coding : observation.getCode().getCoding()) {
			if (coding.getSystem().equals("http://loinc.org")) {
				return new LoincId(coding.getCode());
			}
		}
		
		return null;
	}

}
