package org.octri.hpoonfhir.view;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.monarchinitiative.fhir2hpo.hpo.HpoTermWithNegation;

public class PhenotypeModel implements Serializable {
	
	private static final long serialVersionUID = 5911092102035916719L;

	private final String hpoTermName;
	private final String hpoTermId;
	private final String first;
	private final String last;
	private final List<ObservationModel> observations;
	
	public PhenotypeModel(HpoTermWithNegation hpoTerm, List<ObservationModel> observations) {
		this.hpoTermName = hpoTerm.toString();
		this.hpoTermId = hpoTerm.getHpoTerm().getId().getIdWithPrefix();
		this.observations = observations;
		this.first = observations.stream().filter(observation -> !StringUtils.isBlank(observation.getDate())).reduce((x, y) -> x.getDate().compareTo(y.getDate()) < 0 ? x:y).get().getDate();
		this.last = observations.stream().filter(observation -> !StringUtils.isBlank(observation.getDate())).reduce((x, y) -> x.getDate().compareTo(y.getDate()) < 0 ? y:x).get().getDate();
	}

	public String getHpoTermName() {
		return hpoTermName;
	}
	
	public String getHpoTermId() {
		return hpoTermId;
	}

	public String getFirst() {
		return first;
	}
	
	public String getLast() {
		return last;
	}
	
	public List<ObservationModel> getObservations() {
		return observations;
	}
	
	public Integer getCount() {
		return observations.size();
	}

}
