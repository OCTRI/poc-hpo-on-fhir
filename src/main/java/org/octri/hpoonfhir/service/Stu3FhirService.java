package org.octri.hpoonfhir.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.exceptions.FHIRException;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;


/**
 * The implementation of the STU3 FHIR service. No conversions are necessary.
 * 
 * @author yateam
 *
 */
public class Stu3FhirService extends AbstractFhirService {
	
	private static final FhirContext ctx = FhirContext.forDstu3();

	public Stu3FhirService(String url) {
		super(url);
	}

	@Override
	public FhirContext getFhirContext() {
		return ctx;
	}
	
	@Override
	public Patient findPatientById(String id) throws FHIRException {
		return getClient().read().resource(Patient.class).withId(id).execute();
	}

	@Override
	public List<Patient> findPatientsByFullName(String firstName, String lastName) throws FHIRException {
		//TODO: Check for a next link and more bundles
		Bundle patientBundle = getClient().search().forResource(Patient.class).where(Patient.FAMILY.matches().value(lastName)).and(Patient.GIVEN.matches().value(firstName)).returnBundle(Bundle.class).execute();
		return processPatientBundle(patientBundle);
	}
	
	@Override
	public List<Observation> findObservationsForPatient(String patientId) throws FHIRException {
		List<Observation> allObservations = new ArrayList<>();
		Bundle observationBundle = getClient().search().forResource(Observation.class).where(new ReferenceClientParam("patient").hasId(patientId)).returnBundle(Bundle.class).execute();
		
		while (observationBundle != null) {
			allObservations.addAll(processObservationBundle(observationBundle));
			observationBundle = (observationBundle.getLink(Bundle.LINK_NEXT) != null) ? getClient().loadPage().next(observationBundle).execute() : null;
		}
		
		return allObservations;
	}

	private List<Patient> processPatientBundle(Bundle patientBundle) {
		if (!patientBundle.hasTotal() || patientBundle.getTotal() > 0) {
			return patientBundle.getEntry().stream().map(bundleEntryComponent -> (Patient) bundleEntryComponent.getResource()).collect(Collectors.toList());
		}
		
		return new ArrayList<>();
	}

	private List<Observation> processObservationBundle(Bundle observationBundle) {
		if (!observationBundle.hasTotal() || observationBundle.getTotal() > 0) {
			return observationBundle.getEntry().stream().map(bundleEntryComponent -> (Observation) bundleEntryComponent.getResource()).collect(Collectors.toList());
		}
		
		return new ArrayList<>();
	}

}
