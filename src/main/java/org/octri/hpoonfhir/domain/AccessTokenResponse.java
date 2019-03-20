package org.octri.hpoonfhir.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties({ "token_type", "expires_in", "scope" })
public class AccessTokenResponse {

	private String accessToken;
	private String patient;

	@JsonGetter("access_token")
	public String getAccessToken() {
		return accessToken;
	}

	@JsonSetter("access_token")
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	@JsonGetter("patient")
	public String getPatient() {
		return patient;
	}

	@JsonSetter("patient")
	public void setPatient(String patient) {
		this.patient = patient;
	}

}
