package org.octri.hpoonfhir.service;

import java.util.List;

import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.exceptions.FHIRException;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * The service to query the FHIR server. STU2 and STU3 services set their own context and implement all
 * service methods, return STU3 entities.
 */
public abstract class FhirService {
	
	private final String url;
	private final IGenericClient client;
	
	public abstract FhirContext getFhirContext();
	
	public FhirService(String url) {
		this.url = url;
		getFhirContext().getRestfulClientFactory().setSocketTimeout(30 * 1000); // Extend the timeout
		client = getFhirContext().newRestfulGenericClient(url);
	}
	
	public String getUrl() {
		return url;
	}

	public IGenericClient getClient() {
		return client;
	}
	
	public String getVersion() {
		return client.getFhirContext().getVersion().getVersion().name();
	}
	
	/**
	 * Return a list of patients that loosely match the last name provided.
	 * @param lastName
	 * @return the list of patients
	 */
	public abstract List<Patient> findPatientsByLastName(String lastName) throws FHIRException;

	/**
	 * Return a list of patients that loosely match the first and last name provided.
	 * @param firstName
	 * @param lastName
	 * @return the list of patients
	 */
	public abstract List<Patient> findPatientsByFullName(String firstName, String lastName) throws FHIRException;

}
