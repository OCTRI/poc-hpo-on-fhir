package org.octri.hpoonfhir.view;

import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Observation;
import org.monarchinitiative.fhir2hpo.hpo.HpoConversionResult;

/**
 * This builder takes care of parsing the observation and the conversion result to construct the view.
 * 
 * @author yateam
 *
 */
public class ObservationModelBuilder {
	
	public static ObservationModel build(Observation fhirObservation, List<HpoConversionResult> conversionResults) {
		ObservationModel observationModel = new ObservationModel();
		observationModel.setDescription(fhirObservation.getCode().getCodingFirstRep().getDisplay());
		observationModel.setLoincIds(conversionResults.stream().map(it->it.getLoincId().toString()).collect(Collectors.joining(", ")));
		observationModel.setValue("TODO");
		List<HpoConversionResult> successes = conversionResults.stream().filter(result -> result.hasSuccess()).collect(Collectors.toList());
		String result = successes.size() == 0 ? "Not Found": successes.stream().flatMap(success -> success.getHpoTerms().stream().map(term -> term.toString())).collect(Collectors.joining(", "));
		observationModel.setHpoTerm(result);
		return observationModel;
	}

}
