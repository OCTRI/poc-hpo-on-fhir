package org.octri.hpoonfhir.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Observation;
import org.monarchinitiative.fhir2hpo.fhir.util.ObservationLoincInfo;
import org.monarchinitiative.fhir2hpo.fhir.util.ObservationUtil;
import org.monarchinitiative.fhir2hpo.hpo.HpoTermWithNegation;
import org.monarchinitiative.fhir2hpo.hpo.InferredConversionResult;
import org.monarchinitiative.fhir2hpo.hpo.LoincConversionResult;
import org.monarchinitiative.fhir2hpo.hpo.ObservationConversionResult;
import org.monarchinitiative.fhir2hpo.loinc.LoincId;
import org.monarchinitiative.fhir2hpo.loinc.exception.MismatchedLoincIdException;
import org.monarchinitiative.fhir2hpo.service.HpoService;
import org.monarchinitiative.fhir2hpo.service.ObservationAnalysisService;
import org.monarchinitiative.phenol.ontology.data.Term;
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
		Map<HpoTermWithNegation, List<String>> inferencesByPhenotype = new HashMap<>();
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
				try {
					// TODO: The creation of an ObservationLoincInfo here is very contrived, but we need some way to
					// tell the phenotype model to consider the dates for this observation when determining
					// the phenotype start and end dates
					// Get the main LOINC in the code section of the observation
					LoincId mainLoinc = ObservationUtil.getCodeSectionLoincIdsOfObservation(result.getObservation()).iterator().next();
					ObservationLoincInfo observationLoincInfo = new ObservationLoincInfo(mainLoinc, result.getObservation());
					LoincObservationModel observationModel = new LoincObservationModel(mainLoinc.getCode(), observationLoincInfo);
					List<LoincObservationModel> observations = observationsByPhenotype.get(inferredResult.getHpoTerm());
					
					if (observations == null) {
						observations = new ArrayList<>(Arrays.asList(observationModel));					
					} else {
						observations.add(observationModel);
					}
					observationsByPhenotype.put(inferredResult.getHpoTerm(), observations);
					
					List<String> inferences = inferencesByPhenotype.get(inferredResult.getHpoTerm());
					if (inferences == null) {
						inferences = new ArrayList<>(Arrays.asList(inferredResult.getDescription()));
					} else {
						inferences.add(inferredResult.getDescription());
					}
					inferencesByPhenotype.put(inferredResult.getHpoTerm(), inferences);
				} catch (MismatchedLoincIdException e) {
					// This can't happen because we extracted the LOINC from the observation before creating a new ObservationLoincInfo
				}
			}

			
		}
		
		List<PhenotypeModel> phenotypes = new ArrayList<>();
		for (HpoTermWithNegation term : observationsByPhenotype.keySet()) {
			List<LoincObservationModel> observations = observationsByPhenotype.get(term);
			List<String> inferences = inferencesByPhenotype.get(term);
			// Sort observations by start date
			Collections.sort(observations, (x, y) -> x.getStartDate().compareTo(y.getStartDate()));
			// Get the term information from the HPO service
			Term termInfo = hpoService.getTermForTermId(term.getHpoTermId());
			phenotypes.add(new PhenotypeModel(term, termInfo, observations, inferences));
		}
				
		// Sort phenotypes by name
		Collections.sort(phenotypes, (x, y) -> x.getHpoTermName().compareTo(y.getHpoTermName()));
		return phenotypes;
		
	}

}
