package org.octri.hpoonfhir.view;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.exceptions.FHIRException;

/**
 * The model representing the fields on the observation that will be displayed.
 * 
 * @author yateam
 *
 */
public class ObservationModel implements Serializable {
	
	private static final long serialVersionUID = -3076629390459562137L;
	private static final Logger logger = LogManager.getLogger();
	
	private final String fhirId;
	private final String loincId;
	private final String description;
	private final String date;
	private final String value;
	
	public ObservationModel(String loincId, Observation fhirObservation) {
		this.fhirId = fhirObservation.getIdElement().getIdPart();
		this.loincId = loincId;
		this.description = fhirObservation.getCode().getText();
		this.date = getDateString(fhirObservation);
		this.value = getValueString(fhirObservation);
		
	}
	
	private String getDateString(Observation fhirObservation) {
		// TODO: Handle effective period or NPEs?
		try {
			if (fhirObservation.hasEffectiveDateTimeType()) {
				return fhirObservation.getEffectiveDateTimeType().asStringValue();
			}
		} catch (FHIRException e) {
			// This should not occur since we check existence before getting
			e.printStackTrace();
		}

		logger.warn("Could not find a date for the observation.");
		return "";
	}
	
	private String getValueString(Observation fhirObservation) {
		//TODO: Handle other value types
		try {
			if (fhirObservation.hasValueStringType()) {
				return fhirObservation.getValueStringType().asStringValue();
			}
			if (fhirObservation.hasValueQuantity()) {
				return fhirObservation.getValueQuantity().getValue() + " " + fhirObservation.getValueQuantity().getUnit();
			}
		} catch (FHIRException e) {
			// This should not occur since we check existence before getting
			e.printStackTrace();
		}
		
		logger.warn("Unhandled value type: " + fhirObservation.getValue().getClass());
		return "";
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
