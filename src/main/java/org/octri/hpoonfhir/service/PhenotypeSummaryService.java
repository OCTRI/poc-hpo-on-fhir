package org.octri.hpoonfhir.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.hl7.fhir.r5.model.Annotation;
import org.hl7.fhir.r5.model.BooleanType;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.Observation;
import org.hl7.fhir.r5.model.Reference;
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
 * This service is responsible for converting observations, summarizing the phenotypes found, and building new phenotype observations
 * @author yateam
 *
 */
@Service
public class PhenotypeSummaryService {
	
	public static final String PHENOPACKETS_URL = "http://phenopackets.org/core-ig/master/CodeSystem-hpo.html";
	public static final String PHENOPACKETS_OBSERVATION_CATEGORY = "phenotype";
	private static final String PHENOPACKETS_OBSERVATION_CATEGORY_DISPLAY = "Phenotype";
	
	@Autowired
	ObservationAnalysisService observationAnalysisService;
	
	@Autowired
	HpoService hpoService;
	
	public List<PhenotypeModel> summarizePhenotypes(List<Observation> fhirObservations) {
		
		// Go through the FHIR observations looking for any HPOs already identified
		Map<String,Observation> hpoObservations = new HashMap<>();
		for (Observation fhirObservation : fhirObservations) {
			List<String> hpoCodings = fhirObservation.getCategory().stream().map(it -> it.getCode(PHENOPACKETS_URL)).collect(Collectors.toList());
			if (hpoCodings.contains(PHENOPACKETS_OBSERVATION_CATEGORY)) {
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
				//if (!term.isNegated()) {
					
					// See if the term has already been reported
					Boolean reported = false;
					Observation hpoObservation = hpoObservations.get(term.getHpoTermId().getIdWithPrefix());
					if (hpoObservation != null) {
						reported = hpoObservation.getDerivedFrom().stream().map(it -> it.getReferenceElement().getIdPart()).anyMatch(id -> id.equals(result.getObservationLoincInfo().getFhirId()));
					}
					
					ObservationModel observationModel = new ObservationModel(result.getLoincId().getCode(), result.getObservationLoincInfo(), reported);
					List<ObservationModel> observations = observationsByPhenotype.get(term);
					
					if (observations == null) {
						observations = new ArrayList<>(Arrays.asList(observationModel));					
					} else {
						observations.add(observationModel);
					}
					observationsByPhenotype.put(term, observations);
				//}
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
	
	/**
	 * Build up a phenotype observation to be persisted
	 * @param patientId
	 * @param hpoTermId
	 * @param hpoTermName
	 * @param observations
	 * @param comments
	 * @return the unpersisted observation
	 */
	public static Observation buildPhenotypeObservation(String patientId, String hpoTermId, String hpoTermName,
			String observations, String comments) {
		Observation hpoObservation = new Observation();
		hpoObservation.setId(String.valueOf(new Random().nextLong()));
		
		Identifier identifier = new Identifier().setSystem(PhenotypeSummaryService.PHENOPACKETS_URL).setValue(patientId + "|" + hpoTermId);
		hpoObservation.getIdentifier().add(identifier);
		
		hpoObservation
			.getCode()
			.setText(hpoTermName)
			.addCoding()
			.setSystem(PhenotypeSummaryService.PHENOPACKETS_URL)
			.setCode(hpoTermId)
			.setDisplay(hpoTermName);
        BooleanType btype = new BooleanType(true);
        hpoObservation.setValue(btype);
        
        Reference patientReference = new Reference("Patient/" + patientId);
        hpoObservation.setSubject(patientReference);

        // Parse the observation ids, and get the corresponding observations from FHIR server
		List<String> observationIds = Arrays.asList(observations.split(","));
		for (String observationId : observationIds) {
			Reference reference = new Reference("Observation/" + observationId);
			hpoObservation.getDerivedFrom().add(reference);
		}

        // Add the comment as a note
		Annotation annotation = new Annotation();
        annotation.setText(comments);
        hpoObservation.getNote().add(annotation);
        
        // Add a custom category - may be able to query with this
        CodeableConcept category = new CodeableConcept();
        category.addCoding(PHENOPACKETS_URL, PHENOPACKETS_OBSERVATION_CATEGORY, PHENOPACKETS_OBSERVATION_CATEGORY_DISPLAY);
        hpoObservation.getCategory().add(category);
		return hpoObservation;
	}



}
