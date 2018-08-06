package org.octri.hpoonfhir.view;


/**
 * The model representing the fields on the observation that will be displayed.
 * 
 * @author yateam
 *
 */
public class ObservationModel {
	
	private String description;
	private String loincIds;
	private String value;
	private String hpoTerm;
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getLoincIds() {
		return loincIds;
	}
	
	public void setLoincIds(String loincIds) {
		this.loincIds = loincIds;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getHpoTerm() {
		return hpoTerm;
	}
	
	public void setHpoTerm(String hpoTerm) {
		this.hpoTerm = hpoTerm;
	}	

}
