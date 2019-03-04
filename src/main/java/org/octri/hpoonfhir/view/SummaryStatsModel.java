package org.octri.hpoonfhir.view;

import java.io.Serializable;
import java.util.List;

import org.monarchinitiative.fhir2hpo.hpo.ObservationConversionResult;
import org.monarchinitiative.fhir2hpo.loinc.exception.LoincNotAnnotatedException;

/**
 * The model representing the summary statistics given a list of conversion results
 * 
 * @author yateam
 *
 */
public class SummaryStatsModel implements Serializable {
	
	private static final long serialVersionUID = 4767067270753564484L;

	private final Integer observationCount;
	private final Long numWithLoinc;
	private final Long numAnnotated;
	private final Long numSuccessful;
	private final Double percentAnnotated;
	private final Double percentSuccessAnnotated;
	
	public SummaryStatsModel(List<ObservationConversionResult> conversionResults) {
		this.observationCount = conversionResults.size();
		this.numWithLoinc = conversionResults.stream().filter(ocr -> ocr.getLoincConversionResults().size() > 0).count();
		this.numAnnotated = conversionResults.stream().filter(ocr -> ocr.getLoincConversionResults().stream().
			anyMatch(lcr -> !lcr.hasException() || !lcr.getException().getClass().equals(LoincNotAnnotatedException.class))).count();
		this.numSuccessful = conversionResults.stream().filter(ocr -> ocr.getLoincConversionResults().stream().
			anyMatch(lcr -> lcr.hasSuccess())).count();
		this.percentAnnotated = new Double(numAnnotated)/observationCount * 100.0;
		this.percentSuccessAnnotated = new Double(this.numSuccessful)/this.numAnnotated * 100.0;
	}

	
	public Integer getObservationCount() {
		return observationCount;
	}

	
	public Long getNumWithLoinc() {
		return numWithLoinc;
	}


	public Long getNumAnnotated() {
		return numAnnotated;
	}

	
	public Long getNumSuccessful() {
		return numSuccessful;
	}

	
	public Double getPercentAnnotated() {
		return percentAnnotated;
	}

	
	public Double getPercentSuccessAnnotated() {
		return percentSuccessAnnotated;
	}


}
