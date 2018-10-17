package org.octri.hpoonfhir.service;

import java.util.List;

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
	 * Find the patient by an identifier.
	 * @param token the authentication token
	 * @param id the patient id
	 * @return the single patient or null if none exists
	 * @throws FHIRException 
	 */
	public Patient findPatientById(String token, String id) throws FHIRException;

	/**
	 * Return a list of patients that loosely match the first and last name provided.
	 * @param token the authentication token
	 * @param firstName the first name
	 * @param lastName the last name
	 * @return the list of patients
	 */
	public List<Patient> findPatientsByFullName(String token, String firstName, String lastName) throws FHIRException;

	/**
	 * Return the observations for the given patient id.
	 * @param the authentication token
	 * @param patientId the id of the patient
	 * @return the list of observations
	 */
	public List<Observation> findObservationsForPatient(String token, String patientId) throws FHIRException;

}
