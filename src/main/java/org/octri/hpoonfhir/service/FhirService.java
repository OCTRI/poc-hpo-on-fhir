package org.octri.hpoonfhir.service;

import java.util.List;

import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Observation;
import org.hl7.fhir.r5.model.Patient;
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
	 * @param id
	 * @return the single patient or null if none exists
	 * @throws FHIRException 
	 */
	public Patient findPatientById(String id) throws FHIRException;

	/**
	 * Return a list of patients that loosely match the first and last name provided.
	 * @param firstName
	 * @param lastName
	 * @return the list of patients
	 */
	public List<Patient> findPatientsByFullName(String firstName, String lastName) throws FHIRException;

	/**
	 * 
	 * @param id
	 * @return
	 * @throws FHIRException
	 */
	Observation findObservationById(String id) throws FHIRException;

	/**
	 * Return the observations for the given patient id.
	 * @param patientId
	 * @return the list of observations
	 */
	public List<Observation> findObservationsForPatient(String patientId) throws FHIRException;

	/**
	 * Return the observations for the given patient id.
	 * @param patientId
	 * @param the code for the category
	 * @return the list of observations
	 */
	public List<Observation> findObservationsForPatientAndCategory(String patientId, String categoryCode) throws FHIRException;

	/**
	 * Create or update a patient
	 * @param patient
	 * @return
	 * @throws FHIRException
	 */
	public Patient createUpdatePatient(Patient patient) throws FHIRException;

	/**
	 * Create or update an observation
	 * @param observation
	 * @return
	 */
	public Observation createUpdateObservation(Observation observation) throws FHIRException;
	
	public void deleteResourceById(IdType id);

}
