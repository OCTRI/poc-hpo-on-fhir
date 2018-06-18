package org.octri.hpoonfhir.service;

import java.util.HashSet;
import java.util.Set;

import org.hl7.fhir.dstu3.model.Observation;
import org.monarchinitiative.fhir2hpo.fhir.util.ObservationUtil;
import org.monarchinitiative.fhir2hpo.hpo.HpoConversionResult;
import org.monarchinitiative.fhir2hpo.hpo.HpoTermWithNegation;
import org.monarchinitiative.fhir2hpo.loinc.Loinc2HpoAnnotation;
import org.monarchinitiative.fhir2hpo.loinc.LoincId;
import org.monarchinitiative.fhir2hpo.service.AnnotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ObservationAnalysisService {

	@Autowired
	AnnotationService annotationService;

	public Set<HpoTermWithNegation> analyzeObservation(Observation observation) {
		try {
			LoincId loincId = ObservationUtil.getLoincIdOfObservation(observation);
			if (loincId != null) {
				Loinc2HpoAnnotation annotation= annotationService.getAnnotations(loincId);
				if (annotation != null) {
					HpoConversionResult result = annotation.convert(observation);
					if (result.hasSuccess()) {
						return result.getHpoTerms();
					}
				}
			}
			
		} catch (Exception e) {
			// Some exception occurred (e.g. term was not found)
			e.printStackTrace();
		}

    return new HashSet<>();
	}

}
