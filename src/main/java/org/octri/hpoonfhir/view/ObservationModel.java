package org.octri.hpoonfhir.view;

import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.exceptions.FHIRException;

/**
 * The model representing the fields on the observation that will be displayed.
 * 
 * @author yateam
 *
 */
public class ObservationModel {
	
	private String fhirId;
	private String loincId;
	private String description;
	private String date;
	private String value;
	
	public ObservationModel(String loincId, Observation fhirObservation) {
		this.fhirId = fhirObservation.getIdElement().getIdPart();
		this.loincId = loincId;
		this.description = fhirObservation.getCode().getCodingFirstRep().getDisplay();
		try {
			this.date = fhirObservation.getEffectiveDateTimeType().asStringValue();
		} catch (FHIRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.value = "TODO";
		
	}
	
	public String getFhirId() {
		return fhirId;
	}

	public void setFhirId(String fhirId) {
		this.fhirId = fhirId;
	}
	
	public String getLoincId() {
		return loincId;
	}
	
	public void setLoincId(String loincId) {
		this.loincId = loincId;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

}
