package org.octri.hpoonfhir.view;

import java.util.List;

import org.monarchinitiative.fhir2hpo.hpo.HpoTermWithNegation;

public class PhenotypeModel {
	
	private HpoTermWithNegation hpoTerm;
	private String first;
	private String last;
	private List<ObservationModel> observations;
	
	public PhenotypeModel(HpoTermWithNegation hpoTerm, List<ObservationModel> observations) {
		this.hpoTerm = hpoTerm;
		this.observations = observations;
		this.first = observations.stream().reduce((x, y) -> x.getDate().compareTo(y.getDate()) < 0 ? x:y).get().getDate();
		this.last = observations.stream().reduce((x, y) -> x.getDate().compareTo(y.getDate()) < 0 ? y:x).get().getDate();
	}

	public HpoTermWithNegation getHpoTerm() {
		return hpoTerm;
	}
	
	public void setHpoTerm(HpoTermWithNegation hpoTerm) {
		this.hpoTerm = hpoTerm;
	}
	
	public String getFirst() {
		return first;
	}
	
	public void setFirst(String first) {
		this.first = first;
	}
	
	public String getLast() {
		return last;
	}
	
	public void setLast(String last) {
		this.last = last;
	}
	
	public List<ObservationModel> getObservations() {
		return observations;
	}
	
	public void setObservations(List<ObservationModel> observations) {
		this.observations = observations;
	}
	
	public Integer getCount() {
		return observations.size();
	}

}
