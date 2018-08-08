package org.octri.hpoonfhir.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Observation;
import org.monarchinitiative.fhir2hpo.hpo.HpoConversionResult;
import org.monarchinitiative.fhir2hpo.hpo.HpoTermWithNegation;
import org.monarchinitiative.fhir2hpo.service.ObservationAnalysisService;
import org.octri.hpoonfhir.view.ObservationModel;
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
	
	public List<PhenotypeModel> summarizePhenotypes(List<Observation> fhirObservations) {
		// Try to convert all observations and gather successes
		List<HpoConversionResult> results = fhirObservations.stream()
				.flatMap(fhirObservation -> 
				observationAnalysisService.analyzeObservation(fhirObservation).stream().map(it -> it))
				.filter(result -> result.hasSuccess())
				.collect(Collectors.toList());
		
		Map<HpoTermWithNegation, List<ObservationModel>> observationsByPhenotype = new HashMap<>();
		for (HpoConversionResult result : results ) {
			for (HpoTermWithNegation term : result.getHpoTerms()) {
				ObservationModel observationModel = new ObservationModel(result.getLoincId().getCode(), result.getObservation());
				List<ObservationModel> observations = observationsByPhenotype.get(term);
				
				if (observations == null) {
					observations = new ArrayList<>(Arrays.asList(observationModel));					
				} else {
					observations.add(observationModel);
				}
				observationsByPhenotype.put(term, observations);
			}			
		}
		
		List<PhenotypeModel> phenotypes = new ArrayList<>();
		for (HpoTermWithNegation term : observationsByPhenotype.keySet()) {
			List<ObservationModel> observations = observationsByPhenotype.get(term);
			// Sort observations by date
			Collections.sort(observations, (x, y) -> x.getDate().compareTo(y.getDate()));
			phenotypes.add(new PhenotypeModel(term, observations));
		}
		
		// Sort phenotypes by name
		Collections.sort(phenotypes, (x, y) -> x.getHpoTermName().compareTo(y.getHpoTermName()));
		return phenotypes;
		
	}

}
