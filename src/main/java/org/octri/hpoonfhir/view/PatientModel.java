package org.octri.hpoonfhir.view;

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
	
	public PatientModel(String id, String firstName, String lastName) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
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
