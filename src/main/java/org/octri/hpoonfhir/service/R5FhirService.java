package org.octri.hpoonfhir.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Bundle.BundleLinkComponent;
import org.hl7.fhir.r5.model.Observation;
import org.hl7.fhir.r5.model.Patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;


/**
 * The implementation of the R5 FHIR service. No conversions are necessary.
 * 
 * @author yateam
 *
 */
public class R5FhirService extends AbstractFhirService {
	
	private static final FhirContext ctx = FhirContext.forR5();

	public R5FhirService(String serviceName, String url) {
		super(serviceName, url);
	}

	@Override
	public FhirContext getFhirContext() {
		return ctx;
	}
	
	@Override
	public Patient findPatientById(String id) throws FHIRException {
		try {
			//TODO: AEY This has to be parsed as a long to work with the HAPI FHIR server cause dumb.
			return getClient().read().resource(Patient.class).withId(Long.parseLong(id)).execute();
			//return getClient().read().resource(Patient.class).withId(id).execute();
		} catch(ResourceNotFoundException e) {
			return null;
		}
	}

	@Override
	public List<Patient> findPatientsByFullName(String firstName, String lastName) throws FHIRException {
		//TODO: Check for a next link and more bundles
		Bundle patientBundle = getClient().search().forResource(Patient.class).where(Patient.FAMILY.matches().value(lastName)).and(Patient.GIVEN.matches().value(firstName)).returnBundle(Bundle.class).execute();
		return processPatientBundle(patientBundle);
	}
	
	@Override
	public Observation findObservationById(String id) throws FHIRException {
		try {
			//TODO: AEY This has to be parsed as a long to work with the HAPI FHIR server cause dumb.
			return getClient().read().resource(Observation.class).withId(Long.parseLong(id)).execute();
			//return getClient().read().resource(Observation.class).withId(id).execute();
		} catch(ResourceNotFoundException e) {
			return null;
		}
	}

	@Override
	public List<Observation> findObservationsForPatient(String patientId) throws FHIRException {
		List<Observation> allObservations = new ArrayList<>();
		// TODO: Temp solution for JHU - set the count to 20 to speed up retrievals
		Bundle observationBundle = getClient().search().forResource(Observation.class).where(new ReferenceClientParam("patient").hasId(patientId)).count(50).returnBundle(Bundle.class).execute();
		
		while (observationBundle != null) {
			allObservations.addAll(processObservationBundle(observationBundle));
			// TODO: Temp solution for JHU resources having incorrect URLs
			BundleLinkComponent nextLink = observationBundle.getLink(Bundle.LINK_NEXT);
			if (nextLink != null && nextLink.getUrl().contains("localhost:8080")) {
				String url = nextLink.getUrl();
				observationBundle.getLink(Bundle.LINK_NEXT).setUrl(url.replace("http://localhost:8080", "https://hapi.clinicalprofiles.org"));
			}
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
	
	@Override
	public Patient createUpdatePatient(Patient patient) throws FHIRException {
		Patient found = findPatientById(patient.getIdElement().getIdPart());
		if (found != null) {
			MethodOutcome outcome = getClient().update().resource(patient).execute();
			return (Patient) outcome.getResource();
		}
		
		MethodOutcome outcome = getClient().create().resource(patient).execute();
		return (Patient) outcome.getResource();
	}

	@Override
	public Observation createUpdateObservation(Observation observation) throws FHIRException {
		Observation found = findObservationById(observation.getIdElement().getIdPart());
		if (found != null) {
			MethodOutcome outcome = getClient().update().resource(observation).execute();
			return (Observation) outcome.getResource();
		}
		
		MethodOutcome outcome = getClient().create().resource(observation).execute();
		return (Observation) outcome.getResource();
	}

}
