package org.octri.hpoonfhir.view;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
	private final String startDate;
	private final String endDate;
	private final String value;
	private final Boolean reported;

	public ObservationModel(String loincId, ObservationLoincInfo observationLoincInfo, Boolean reported) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		this.fhirId = observationLoincInfo.getFhirId();
		this.loincId = loincId;
		this.description = observationLoincInfo.getDescription();
		this.startDate = observationLoincInfo.getStartDate().map(d -> df.format(d)).orElse("");
		this.endDate = observationLoincInfo.getEndDate().map(d -> df.format(d)).orElse("");
		this.value = observationLoincInfo.getValueDescription();
		this.reported = reported;
	}

	/**
	 * The Observation FHIR Id
	 * @return
	 */
	public String getFhirId() {
		return fhirId;
	}

	/**
	 * The LOINC Id
	 * @return
	 */
	public String getLoincId() {
		return loincId;
	}

	/**
	 * The description of the LOINC test
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * A string representing the startDate formatted as yyyy-MM-dd. If no start date exists, return the empty string.
	 * @return
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * A string representing the endDate formatted as yyyy-MM-dd. If no end date exists, return the empty string.
	 * @return
	 */
	public String getEndDate() {
		return endDate;
	}

	/**
	 * The description of the value for the LOINC
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Whether this specific Observation has had the HPO Term reported back to the server
	 * @return
	 */
	public Boolean getReported() {
		return reported;
	}

}
