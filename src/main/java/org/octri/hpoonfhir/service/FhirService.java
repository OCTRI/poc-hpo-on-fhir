package org.octri.hpoonfhir.service;

import java.util.List;

import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.exceptions.FHIRException;
import org.hspconsortium.client.session.Session;

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
	 * @param id
	 * @return the single patient or null if none exists
	 * @throws FHIRException 
	 */
	public Patient findPatientById(Session session, String id) throws FHIRException;

	/**
	 * Return a list of patients that loosely match the first and last name provided.
	 * @param accessToken 
	 * @param firstName
	 * @param lastName
	 * @return the list of patients
	 */
	public List<Patient> findPatientsByFullName(Session session, String firstName, String lastName) throws FHIRException;

	/**
	 * Return the observations for the given patient id.
	 * @param patientId
	 * @return the list of observations
	 */
	public List<Observation> findObservationsForPatient(Session session, String patientId) throws FHIRException;

}
