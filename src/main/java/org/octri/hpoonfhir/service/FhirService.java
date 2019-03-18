package org.octri.hpoonfhir.service;

import java.util.List;

import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.exceptions.FHIRException;

/**
 * The service interface to query the FHIR server. While the underlying server may have a different FHIR version,
 * the interface expects STU3 objects to be returned.
 * 
 * @author yateam
 */
public interface FhirService {
	
	/**
	 * Return a user-friendly name for the service
	 * @return
	 */
	public String getServiceName();
	
	/**
	 * The service uri
	 * @return
	 */
	public String getServiceEndpoint();
	
	/**
	 * The endpoint to hit for authorization
	 * @return
	 */
	public String getAuthorizeEndpoint();
	
	/**
	 * The endpoint to hit for token exchange
	 * @return
	 */
	public String getTokenEndpoint();
	
	/**
	 * The redirect uri for the application
	 * @return
	 */
	public String getRedirectUri();
	
	/**
	 * Get the client id for the FHIR server
	 * @return
	 */
	public String getClientId();
	
	/**
	 * Get the client secret for the FHIR server
	 * @return
	 */
	public String getClientSecret();

	/**
	 * Find the patient by an identifier.
	 * @param id
	 * @return the single patient or null if none exists
	 * @throws FHIRException 
	 */
	public Patient findPatientById(String token, String id);

	/**
	 * Return a list of patients that loosely match the first and last name provided.
	 * @param firstName
	 * @param lastName
	 * @return the list of patients
	 */
	public List<Patient> findPatientsByFullName(String token, String firstName, String lastName);

	/**
	 * Return the observations for the given patient id.
	 * @param patientId
	 * @return the list of observations
	 */
	public List<Observation> findObservationsForPatient(String token, String patientId);

	/**
	 * Find the observation by an identifier.
	 * @param id
	 * @return the single observation or null if none exists
	 * @throws FHIRException 
	 */
	public Observation findObservationById(String token, String id);
	
	public List<Condition> findConditionsForPatient(String token, String id);
	
	public List<Condition> findConditionsByCode(String token, String id);

}
