package org.octri.hpoonfhir.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Observation;
import org.monarchinitiative.fhir2hpo.hpo.LoincConversionResult;
import org.monarchinitiative.fhir2hpo.hpo.ObservationConversionResult;
import org.monarchinitiative.fhir2hpo.hpo.InferredConversionResult;
import org.monarchinitiative.fhir2hpo.hpo.HpoTermWithNegation;
import org.monarchinitiative.fhir2hpo.service.HpoService;
import org.monarchinitiative.fhir2hpo.service.ObservationAnalysisService;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.octri.hpoonfhir.view.LoincObservationModel;
import org.octri.hpoonfhir.view.PhenotypeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service is responsible for converting all observations and summarizing the phenotypes found
 * @author yateam
 *
 */
@Service
public class PhenotypeSummaryService {

	@Autowired
	ObservationAnalysisService observationAnalysisService;
	
	@Autowired
	HpoService hpoService;
	
	public List<PhenotypeModel> summarizePhenotypes(List<Observation> fhirObservations) {
		// Try to convert all observations
		List<ObservationConversionResult> results = fhirObservations.stream()
				.map(fhirObservation -> observationAnalysisService.analyzeObservation(fhirObservation))
				.collect(Collectors.toList());
		
		Map<HpoTermWithNegation, List<LoincObservationModel>> observationsByPhenotype = new HashMap<>();
		Map<HpoTermWithNegation, List<LoincObservationModel>> inferencesByPhenotype = new HashMap<>();
		for (ObservationConversionResult result : results ) {
			
			// First go through the direct successful results
			List<LoincConversionResult> successfulLoincResults = result.getLoincConversionResults().stream().filter(r -> r.hasSuccess()).collect(Collectors.toList());			
			for (LoincConversionResult loincResult : successfulLoincResults) {
				for (HpoTermWithNegation term : loincResult.getHpoTerms()) {
					LoincObservationModel observationModel = new LoincObservationModel(loincResult.getLoincId().getCode(), loincResult.getObservationLoincInfo());
					List<LoincObservationModel> observations = observationsByPhenotype.get(term);
					
					if (observations == null) {
						observations = new ArrayList<>(Arrays.asList(observationModel));					
					} else {
						observations.add(observationModel);
					}
					observationsByPhenotype.put(term, observations);
				}
			}
			
			// Now go through the inferred results
			List<InferredConversionResult> inferredConversionResults = result.getInferredConversionResults();
			for (InferredConversionResult inferredResult : inferredConversionResults) {
				// TODO: Create an ObservationModel using any successful LoincResult. This allows us to use the 
				// date parsing logic needed for display. We need a better solution than this.
				LoincConversionResult loincResult = successfulLoincResults.get(0);
				LoincObservationModel observationModel = new LoincObservationModel(loincResult.getLoincId().getCode(), loincResult.getObservationLoincInfo());
				List<LoincObservationModel> observations = inferencesByPhenotype.get(inferredResult.getHpoTerm());
				
				if (observations == null) {
					observations = new ArrayList<>(Arrays.asList(observationModel));					
				} else {
					observations.add(observationModel);
				}
				inferencesByPhenotype.put(inferredResult.getHpoTerm(), observations);
			}

			
		}
		
		List<PhenotypeModel> phenotypes = new ArrayList<>();
		for (HpoTermWithNegation term : observationsByPhenotype.keySet()) {
			List<LoincObservationModel> observations = observationsByPhenotype.get(term);
			// Sort observations by start date
			Collections.sort(observations, (x, y) -> x.getStartDate().compareTo(y.getStartDate()));
			// Get the term information from the HPO service
			Term termInfo = hpoService.getTermForTermId(term.getHpoTermId());
			phenotypes.add(new PhenotypeModel(term, termInfo, observations));
		}
		

		for (HpoTermWithNegation term : inferencesByPhenotype.keySet()) {
			List<LoincObservationModel> observations = inferencesByPhenotype.get(term);
			// Sort observations by start date
			Collections.sort(observations, (x, y) -> x.getStartDate().compareTo(y.getStartDate()));
			// Get the term information from the HPO service
			Term termInfo = hpoService.getTermForTermId(term.getHpoTermId());
			phenotypes.add(new PhenotypeModel(term, termInfo, observations));
		}
		
		// Sort phenotypes by name
		Collections.sort(phenotypes, (x, y) -> x.getHpoTermName().compareTo(y.getHpoTermName()));
		return phenotypes;
		
	}

}
