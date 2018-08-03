package org.octri.hpoonfhir.view;

/**
 * The model representing the fields we care about when search for and displaying a patient
 * 
 * @author yateam
 *
 */
public class PatientModel {

	
	private String id;
	private String firstName;
	private String lastName;
	
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
