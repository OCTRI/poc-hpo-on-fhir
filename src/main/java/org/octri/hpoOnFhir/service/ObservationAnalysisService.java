package org.octri.hpoOnFhir.service;

import java.util.HashSet;
import java.util.Set;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Observation;
import org.monarchinitiative.loinc2hpo.exception.LoincCodeNotAnnotatedException;
import org.monarchinitiative.loinc2hpo.exception.LoincCodeNotFoundException;
import org.monarchinitiative.loinc2hpo.exception.MalformedLoincCodeException;
import org.monarchinitiative.loinc2hpo.exception.UnsupportedCodingSystemException;
import org.monarchinitiative.loinc2hpo.fhir.FhirObservationAnalyzer;
import org.monarchinitiative.loinc2hpo.fhir.ObservationAnalysisFromCodedValues;
import org.monarchinitiative.loinc2hpo.fhir.ObservationAnalysisFromInterpretation;
import org.monarchinitiative.loinc2hpo.fhir.ObservationAnalysisFromQnValue;
import org.monarchinitiative.loinc2hpo.loinc.HpoTerm4TestOutcome;
import org.monarchinitiative.loinc2hpo.loinc.LOINC2HpoAnnotationImpl;
import org.monarchinitiative.loinc2hpo.loinc.LoincId;
import org.monarchinitiative.loinc2hpo.testresult.BasicLabTestOutcome;
import org.monarchinitiative.loinc2hpo.testresult.LabTestOutcome;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ObservationAnalysisService {

	@Autowired
	AnnotationService annotationService;

	// TODO: Should this really return a list of terms? Technically, an
	// observation can have multiple codings,
	// but I doubt this could ever be more than one LOINC
	public Term analyzeObservation(Observation observation) throws InterruptedException {

		// TODO: The analyzer should not need a static declaration of the
		// observation. This is not thread-safe.
		FhirObservationAnalyzer.setObservation(observation);

		// This illustrates the issue with the library not being thread-safe.
		// This method is called nearly simultaneously with all 10
		// observations. The observation gets reset by the static call above
		// before we calculate the outcome for that observation,
		// leading to unpredictable results.
		Thread.sleep(1000);

		// TODO: The Analyzer has the observation, so I should not need to pass
		// loinc ids?
		try {
			Set<LoincId> loincIds = new HashSet<>();
			LoincId loincId = FhirObservationAnalyzer.getLoincIdOfObservation();
			loincIds.add(loincId);
			LabTestOutcome outcome = FhirObservationAnalyzer.getHPO4ObservationOutcome(loincIds,
					annotationService.getAnnotations());
			return outcome.getOutcome().getHpoTerm();
		} catch (Exception e) {
			// Some exception occurred (e.g. term was not found)
			e.printStackTrace();
		}

		return null;

	}

	// Here's a threadsafe implementation where I've extracted the functionality from FHIRObservationAnalyzer. I also
	// made some improvements to the ObservationAnalysis implementations for convenience - in general those
	// impls could use some refactoring to only provide the minimum for analysis and any calls to get additional
	// info about the patient should be avoided within the impls. Those are a different concern.
	public Term analyzeObservationThreadsafe(Observation observation) {
		try {
			LoincId loincId = getLoincIdOfObservation(observation);
			LOINC2HpoAnnotationImpl annotations= annotationService.getAnnotations().get(loincId);
			if (annotations == null) {
				throw new LoincCodeNotAnnotatedException();
			}
			HpoTerm4TestOutcome hpoterm = null;
	        if (observation.hasInterpretation()) {
	            //hpoterm won't be null
	            hpoterm = new ObservationAnalysisFromInterpretation(loincId, observation.getInterpretation(), annotations).getHPOforObservation();
	        }
	        if (observation.hasValueQuantity()) {
	            hpoterm = new ObservationAnalysisFromQnValue(loincId, observation, annotations).getHPOforObservation();
	        }

	        //Ord will have a ValueCodeableConcept field
	        if (observation.hasValueCodeableConcept()) {
	            hpoterm = new ObservationAnalysisFromCodedValues(loincId,
	                    observation.getValueCodeableConcept(), annotations).getHPOforObservation();
	        }

            if (hpoterm != null) {
            	LabTestOutcome labTestOutcome = new BasicLabTestOutcome(hpoterm, null, observation.getSubject(), observation.getIdentifier());
            	return labTestOutcome.getOutcome().getHpoTerm();
            }

		} catch (Exception e) {
			// Some exception occurred (e.g. term was not found)
			e.printStackTrace();
		}
        
        return null;
	}

	public static LoincId getLoincIdOfObservation(Observation observation) throws MalformedLoincCodeException, LoincCodeNotFoundException, UnsupportedCodingSystemException {

		// TODO: Should this exit out after finding first loinc id or return a list?
		for (Coding coding : observation.getCode().getCoding()) {
			if (coding.getSystem().equals("http://loinc.org")) {
				return new LoincId(coding.getCode());
			}
		}
		
		throw new LoincCodeNotFoundException();
	}

}
