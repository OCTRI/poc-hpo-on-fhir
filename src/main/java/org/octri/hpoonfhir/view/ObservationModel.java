package org.octri.hpoonfhir.view;

import java.io.Serializable;

import org.monarchinitiative.fhir2hpo.fhir.util.ObservationLoincInfo;

/**
 * The model representing the fields on the observation that will be displayed.
 * 
 * @author yateam
 *
 */
public class ObservationModel implements Serializable {

	private static final long serialVersionUID = -3076629390459562137L;

	private final String fhirId;
	private final String loincId;
	private final String description;
	private final String date;
	private final String value;

	public ObservationModel(String loincId, ObservationLoincInfo observationLoincInfo) {
		this.fhirId = observationLoincInfo.getFhirId();
		this.loincId = loincId;
		this.description = observationLoincInfo.getDescription();
		this.date = observationLoincInfo.getDate();
		this.value = observationLoincInfo.getValueDescription();

	}

	public String getFhirId() {
		return fhirId;
	}

	public String getLoincId() {
		return loincId;
	}

	public String getDescription() {
		return description;
	}

	public String getDate() {
		return date;
	}

	public String getValue() {
		return value;
	}

}
