package org.octri.hpoonfhir.view;

import org.hl7.fhir.r5.model.Patient;

/**
 * The model representing the fields we care about when searching for and displaying a patient
 * 
 * @author yateam
 *
 */
public class PatientModel {

	
	private String id;
	private String firstName;
	private String lastName;
	
	public PatientModel() {
		
	}
	
	public PatientModel(Patient fhirPatient) {
		this.id = fhirPatient.getIdElement().getIdPart();
		this.firstName = fhirPatient.getNameFirstRep().getGivenAsSingleString();
		this.lastName = fhirPatient.getNameFirstRep().getFamily();
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

}
