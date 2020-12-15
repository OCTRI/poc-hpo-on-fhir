package org.octri.hpoonfhir.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.convertors.VersionConvertor_30_50;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Observation;
import org.hl7.fhir.r5.model.Patient;

import ca.uhn.fhir.context.FhirContext;


/**
 * The implementation of the STU3 FHIR service. No conversions are necessary.
 * 
 * @author yateam
 *
 */
public class Stu3FhirService extends AbstractFhirService {
	
	private static final FhirContext ctx = FhirContext.forDstu3();

	public Stu3FhirService(String serviceName, String url) {
		super(serviceName, url);
	}

	@Override
	public FhirContext getFhirContext() {
		return ctx;
	}
	
	@Override
	public Patient findPatientById(String id) throws FHIRException {
		org.hl7.fhir.dstu3.model.Patient stu3Patient = getClient().read().resource(org.hl7.fhir.dstu3.model.Patient.class).withId(id).execute();		
		return (Patient) VersionConvertor_30_50.convertResource(stu3Patient, true);
	}

	@Override
	public List<Patient> findPatientsByFullName(String firstName, String lastName) throws FHIRException {
		org.hl7.fhir.dstu3.model.Bundle patientBundle = getClient().search().forResource(org.hl7.fhir.dstu3.model.Patient.class).where(Patient.FAMILY.matches().value(lastName)).and(Patient.GIVEN.matches().value(firstName)).returnBundle(org.hl7.fhir.dstu3.model.Bundle.class).execute();
		return processPatientBundle(patientBundle);
		
	}
	
	@Override
	public Observation findObservationById(String id) throws FHIRException {
		throw new NotImplementedException("Only available on R5 servers");
	}

	@Override
	public List<Observation> findObservationsForPatient(String patientId) throws FHIRException {
		List<Observation> allObservations = new ArrayList<>();
		// Epic sandbox query will fail if category is not provided
		org.hl7.fhir.dstu3.model.Bundle observationBundle = getClient().search()
				.byUrl("Observation?patient=" + patientId + "&category=vital-signs,laboratory")
				.returnBundle(org.hl7.fhir.dstu3.model.Bundle.class).execute();
		
		while (observationBundle != null) {
			allObservations.addAll(processObservationBundle(observationBundle));
			observationBundle = (observationBundle.getLink(Bundle.LINK_NEXT) != null) ? getClient().loadPage().next(observationBundle).execute() : null;
		}
		
		return allObservations;
	}

	private List<Patient> processPatientBundle(org.hl7.fhir.dstu3.model.Bundle patientBundle) throws FHIRException {
		List<Patient> upgradedPatients = new ArrayList<>();
		if (!patientBundle.hasTotal() || patientBundle.getTotal() > 0) {
			for (org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent bundleEntryComponent: patientBundle.getEntry()) {
				org.hl7.fhir.dstu3.model.Patient stu3Patient = (org.hl7.fhir.dstu3.model.Patient) bundleEntryComponent.getResource();
				Patient upgradedPatient = (Patient) VersionConvertor_30_50.convertResource(stu3Patient, true);
				upgradedPatients.add(upgradedPatient);
			}
		}

		return upgradedPatients;		
	}

	private List<Observation> processObservationBundle(org.hl7.fhir.dstu3.model.Bundle observationBundle) throws FHIRException {
		List<Observation> upgradedObservations = new ArrayList<>();
		if (!observationBundle.hasTotal() || observationBundle.getTotal() > 0) {
			for (org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent bundleEntryComponent: observationBundle.getEntry()) {
				org.hl7.fhir.dstu3.model.Observation stu3Observation = (org.hl7.fhir.dstu3.model.Observation) bundleEntryComponent.getResource();
				Observation upgradedObservation = (Observation) VersionConvertor_30_50.convertResource(stu3Observation, true);
				upgradedObservations.add(upgradedObservation);
			}
		}
		
		return upgradedObservations;		
	}

	@Override
	public Patient createUpdatePatient(Patient patient) throws FHIRException {
		throw new NotImplementedException("Only available on R5 servers");
	}

	@Override
	public Observation createUpdateObservation(Observation observation) throws FHIRException {
		throw new NotImplementedException("Only available on R5 servers");
	}

}
