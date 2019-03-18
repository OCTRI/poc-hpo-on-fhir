package org.octri.hpoonfhir.view;

import org.hl7.fhir.dstu3.model.Condition;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

/**
 * The model representing the fields we care about when searching for and displaying a condition
 * 
 * @author yateam
 *
 */
public class ConditionModel {

	private String id;
	private String code;
	private String description;
	private String json;
	private String patient;

	public ConditionModel() {

	}

	public ConditionModel(Condition fhirCondition, IParser parser) {
		this.id = fhirCondition.getIdElement().getIdPart();
		this.code = fhirCondition.getCode().getCoding().get(0).getCode();
		this.description = fhirCondition.getCode().getCoding().get(0).getDisplay();
		this.json = parser.encodeResourceToString(fhirCondition);
		this.setPatient(fhirCondition.getSubject().getReference().split("/")[1]);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public String getPatient() {
		return patient;
	}

	public void setPatient(String patient) {
		this.patient = patient;
	}

}
