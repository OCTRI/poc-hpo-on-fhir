package org.octri.hpoonfhir.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.fhir.r5.model.Observation;
import org.monarchinitiative.fhir2hpo.hpo.HpoTermWithNegation;
import org.monarchinitiative.fhir2hpo.hpo.LoincConversionResult;
import org.monarchinitiative.fhir2hpo.service.HpoService;
import org.monarchinitiative.fhir2hpo.service.ObservationAnalysisService;
import org.monarchinitiative.phenol.ontology.data.Term;
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
	
	@Autowired
	HpoService hpoService;
	
	public List<PhenotypeModel> summarizePhenotypes(List<Observation> fhirObservations) {
		
		// Go through the FHIR observations looking for any HPOs already identified
		Map<String,Observation> hpoObservations = new HashMap<>();
		for (Observation fhirObservation : fhirObservations) {
			List<String> hpoCodings = fhirObservation.getCategory().stream().map(it -> it.getCode("http://hpo.jax.org")).collect(Collectors.toList());
			if (hpoCodings.contains("hpo")) {
				String hpoCode = fhirObservation.getCode().getCoding().get(0).getCode();
				hpoObservations.put(hpoCode, fhirObservation);
			}
		}
		
		// Try to convert observations to HPO and gather successes
		List<LoincConversionResult> results = fhirObservations.stream()
				.flatMap(fhirObservation -> observationAnalysisService.analyzeObservation(fhirObservation).getLoincConversionResults().stream())
				.filter(result -> result.hasSuccess())
				.collect(Collectors.toList());
		
		Map<HpoTermWithNegation, List<ObservationModel>> observationsByPhenotype = new HashMap<>();
		for (LoincConversionResult result : results ) {
			for (HpoTermWithNegation term : result.getHpoTerms()) {
				// Ignore negated terms - usually indicates the absence of a condition
				if (!term.isNegated()) {
					
					// See if the term has already been reported
					Boolean reported = false;
					Observation hpoObservation = hpoObservations.get(term.getHpoTermId().getIdWithPrefix());
					if (hpoObservation != null) {
						reported = hpoObservation.getDerivedFrom().stream().map(it -> it.getResource().getIdElement().getIdPart()).anyMatch(id -> id.equals(result.getObservationLoincInfo().getFhirId()));
					}
					
					ObservationModel observationModel = new ObservationModel(result.getLoincId().getCode(), result.getObservationLoincInfo(), reported);
					List<ObservationModel> observations = observationsByPhenotype.get(term);
					
					if (observations == null) {
						observations = new ArrayList<>(Arrays.asList(observationModel));					
					} else {
						observations.add(observationModel);
					}
					observationsByPhenotype.put(term, observations);
				}
			}			
		}
		
		List<PhenotypeModel> phenotypes = new ArrayList<>();
		for (HpoTermWithNegation term : observationsByPhenotype.keySet()) {
			List<ObservationModel> observations = observationsByPhenotype.get(term);
			// Sort observations by start date
			Collections.sort(observations, (x, y) -> x.getStartDate().compareTo(y.getStartDate()));
			// Get the term information from the HPO service
			Term termInfo = hpoService.getTermForTermId(term.getHpoTermId());
			phenotypes.add(new PhenotypeModel(term, termInfo, observations, hpoObservations.get(term.getHpoTermId().getIdWithPrefix())));
		}
		
		// Sort phenotypes by name
		Collections.sort(phenotypes, (x, y) -> x.getHpoTermName().compareTo(y.getHpoTermName()));
		return phenotypes;
		
	}

}
