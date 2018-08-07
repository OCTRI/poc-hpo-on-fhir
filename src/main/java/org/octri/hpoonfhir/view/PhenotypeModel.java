package org.octri.hpoonfhir.view;

import java.io.Serializable;
import java.util.List;

import org.monarchinitiative.fhir2hpo.hpo.HpoTermWithNegation;

public class PhenotypeModel implements Serializable {
	
	private static final long serialVersionUID = 5911092102035916719L;

	private String hpoTermDisplay;
	private String first;
	private String last;
	private List<ObservationModel> observations;
	
	public PhenotypeModel(HpoTermWithNegation hpoTerm, List<ObservationModel> observations) {
		this.hpoTermDisplay = hpoTerm.toString() + " (" + hpoTerm.getHpoTerm().getId().getIdWithPrefix() + ")";
		this.observations = observations;
		this.first = observations.stream().reduce((x, y) -> x.getDate().compareTo(y.getDate()) < 0 ? x:y).get().getDate();
		this.last = observations.stream().reduce((x, y) -> x.getDate().compareTo(y.getDate()) < 0 ? y:x).get().getDate();
	}

	public String getHpoTermDisplay() {
		return hpoTermDisplay;
	}
	
	public void setHpoTermDisplay(String hpoTermDisplay) {
		this.hpoTermDisplay = hpoTermDisplay;
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
