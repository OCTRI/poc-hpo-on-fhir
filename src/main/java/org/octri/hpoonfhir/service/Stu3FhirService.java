package org.octri.hpoonfhir.service;

import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.exceptions.FHIRException;

import ca.uhn.fhir.context.FhirContext;


/**
 * The implementation of the STU3 FHIR service. No conversions are necessary.
 * 
 * @author yateam
 *
 */
public class Stu3FhirService extends FhirService {
	
	// This is expensive, so make it static so it's only done once
	private static final FhirContext ctx = FhirContext.forDstu3();

	public Stu3FhirService(String url) {
		super(url);
	}

	@Override
	public FhirContext getFhirContext() {
		return ctx;
	}
	
	@Override
	public List<Patient> findPatientsByLastName(String lastName) throws FHIRException {
		Bundle patientBundle = getClient().search().forResource(Patient.class).where(Patient.FAMILY.matches().value(lastName)).returnBundle(Bundle.class).execute();
		return patientBundle.getEntry().stream().map(bundleEntryComponent -> (Patient) bundleEntryComponent.getResource()).collect(Collectors.toList());
	}

	@Override
	public List<Patient> findPatientsByFullName(String firstName, String lastName) throws FHIRException {
		Bundle patientBundle = getClient().search().forResource(Patient.class).where(Patient.FAMILY.matches().value(lastName)).and(Patient.GIVEN.matches().value(firstName)).returnBundle(Bundle.class).execute();
		return patientBundle.getEntry().stream().map(bundleEntryComponent -> (Patient) bundleEntryComponent.getResource()).collect(Collectors.toList());
	}

}
