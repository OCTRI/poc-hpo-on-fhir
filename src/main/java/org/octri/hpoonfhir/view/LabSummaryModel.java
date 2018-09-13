package org.octri.hpoonfhir.view;

import java.util.List;
import java.util.Map;

import org.monarchinitiative.fhir2hpo.loinc.LoincId;

/**
 * Summary of JHU Aggregate Data for a Given Lab. This include some stats along with
 * HPO Terms if any are mapped.
 * 
 * @author yateam
 *
 */
public class LabSummaryModel {

	private LoincId loincId;
	private Boolean annotated;
	private String description;
	private Double min;
	private Double mean;
	private Double max;
	private Double median;
	private Double fractionAboveNormal;
	private Double fractionBelowNormal;
	private List<Map<String,String>> relatedHpoTerms;
	private ReferenceRange referenceRange;

	public LoincId getLoincId() {
		return loincId;
	}

	public void setLoincId(LoincId loincId) {
		this.loincId = loincId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Double getMin() {
		return min;
	}

	public void setMin(Double min) {
		this.min = min;
	}

	public Double getMean() {
		return mean;
	}

	public void setMean(Double mean) {
		this.mean = mean;
	}

	public Double getMax() {
		return max;
	}

	public void setMax(Double max) {
		this.max = max;
	}

	public Double getFractionAboveNormal() {
		return fractionAboveNormal;
	}

	public void setFractionAboveNormal(Double fractionAboveNormal) {
		this.fractionAboveNormal = fractionAboveNormal;
	}

	public Double getFractionBelowNormal() {
		return fractionBelowNormal;
	}

	public void setFractionBelowNormal(Double fractionBelowNormal) {
		this.fractionBelowNormal = fractionBelowNormal;
	}

	public List<Map<String, String>> getRelatedHpoTerms() {
		return relatedHpoTerms;
	}

	public void setRelatedHpoTerms(List<Map<String, String>> relatedHpoTerms) {
		this.relatedHpoTerms = relatedHpoTerms;
	}

	public Double getMedian() {
		return median;
	}

	public void setMedian(Double median) {
		this.median = median;
	}

	public Boolean getAnnotated() {
		return annotated;
	}

	public void setAnnotated(Boolean annotated) {
		this.annotated = annotated;
	}

	public ReferenceRange getReferenceRange() {
		return referenceRange;
	}

	public void setReferenceRange(ReferenceRange referenceRange) {
		this.referenceRange = referenceRange;
	}

}
