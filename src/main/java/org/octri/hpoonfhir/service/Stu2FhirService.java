package org.octri.hpoonfhir.service;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.convertors.NullVersionConverterAdvisor30;
import org.hl7.fhir.convertors.VersionConvertor_10_30;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.exceptions.FHIRException;

import ca.uhn.fhir.context.FhirContext;


/**
 * The implementation of the STU2 FHIR service. FHIR communication uses STU2 and results are converted to STU3.
 * 
 * @author yateam
 *
 */
public class Stu2FhirService extends AbstractFhirService {
	
	private static final FhirContext ctx = FhirContext.forDstu2Hl7Org();
	private final VersionConvertor_10_30 converter = new VersionConvertor_10_30(new NullVersionConverterAdvisor30());

	public Stu2FhirService(String url) {
		super(url);
	}

	@Override
	public FhirContext getFhirContext() {
		return ctx;
	}
	
	@Override
	public Patient findPatientById(String id) throws FHIRException {
		org.hl7.fhir.instance.model.Patient stu2Patient = getClient().read().resource(org.hl7.fhir.instance.model.Patient.class).withId(id).execute();
		return (Patient) converter.convertPatient(stu2Patient);
	}

	@Override
	public List<Patient> findPatientsByFullName(String firstName, String lastName) throws FHIRException {
		org.hl7.fhir.instance.model.Bundle patientBundle = getClient().search().forResource(org.hl7.fhir.instance.model.Patient.class).where(Patient.FAMILY.matches().value(lastName)).and(Patient.GIVEN.matches().value(firstName)).returnBundle(org.hl7.fhir.instance.model.Bundle.class).execute();
		return processPatientBundle(patientBundle);
		
	}
	
	@Override
	public List<Observation> findObservationsForPatient(String patientId) throws FHIRException {
		List<Observation> allObservations = new ArrayList<>();
		// Epic sandbox query will fail if category is not provided
		org.hl7.fhir.instance.model.Bundle observationBundle = getClient().search()
				.byUrl("Observation?patient=" + patientId + "&category=vital-signs,laboratory")
				.returnBundle(org.hl7.fhir.instance.model.Bundle.class).execute();
		
		while (observationBundle != null) {
			allObservations.addAll(processObservationBundle(observationBundle));
			observationBundle = (observationBundle.getLink(Bundle.LINK_NEXT) != null) ? getClient().loadPage().next(observationBundle).execute() : null;
		}
		
		return allObservations;
	}

	private List<Patient> processPatientBundle(org.hl7.fhir.instance.model.Bundle patientBundle) throws FHIRException {
		List<Patient> stu3Patients = new ArrayList<>();
		if (!patientBundle.hasTotal() || patientBundle.getTotal() > 0) {
			for (org.hl7.fhir.instance.model.Bundle.BundleEntryComponent bundleEntryComponent: patientBundle.getEntry()) {
				org.hl7.fhir.instance.model.Patient stu2Patient = (org.hl7.fhir.instance.model.Patient) bundleEntryComponent.getResource();
				Patient stu3Patient = (Patient) converter.convertPatient(stu2Patient);
				stu3Patients.add(stu3Patient);
			}
		}

		return stu3Patients;		
	}

	private List<Observation> processObservationBundle(org.hl7.fhir.instance.model.Bundle observationBundle) throws FHIRException {
		List<Observation> stu3Observations = new ArrayList<>();
		if (!observationBundle.hasTotal() || observationBundle.getTotal() > 0) {
			for (org.hl7.fhir.instance.model.Bundle.BundleEntryComponent bundleEntryComponent: observationBundle.getEntry()) {
				org.hl7.fhir.instance.model.Observation stu2Observation = (org.hl7.fhir.instance.model.Observation) bundleEntryComponent.getResource();
				Observation stu3Observation = (Observation) converter.convertObservation(stu2Observation);
				stu3Observations.add(stu3Observation);
			}
		}
		
		return stu3Observations;		
	}


}
