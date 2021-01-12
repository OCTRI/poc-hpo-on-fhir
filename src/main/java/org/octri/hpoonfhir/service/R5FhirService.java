package org.octri.hpoonfhir.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Observation;
import org.hl7.fhir.r5.model.Patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import io.micrometer.core.instrument.util.StringUtils;


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
	
	public Bundle searchByUrl(String url) {
		//TODO: Clarify that only first Bundle is returned; not checking for next link
		return getClient().search().byUrl(url).returnBundle(Bundle.class).execute();
	}

	@Override
	public Patient findPatientById(String id) throws FHIRException {
		return getResourceById(Patient.class, id);
	}

	@Override
	public List<Patient> findPatientsByFullName(String firstName, String lastName) throws FHIRException {
		//TODO: Check for a next link and more bundles
		Bundle patientBundle = getClient().search().forResource(Patient.class).where(Patient.FAMILY.matches().value(lastName)).and(Patient.GIVEN.matches().value(firstName)).returnBundle(Bundle.class).execute();
		return processPatientBundle(patientBundle);
	}
	
	@Override
	public Observation findObservationById(String id) throws FHIRException {
		return getResourceById(Observation.class, id);
	}

	@Override
	public List<Observation> findObservationsForPatient(String patientId, String categoryCode)
			throws FHIRException {
		List<Observation> allObservations = new ArrayList<>();
		String categoryParameter = StringUtils.isBlank(categoryCode) ? "" : "&category=" + categoryCode;
		Bundle observationBundle = getClient().search()
				.byUrl("Observation?patient=" + patientId + categoryParameter)
				.returnBundle(Bundle.class).execute();
		while (observationBundle != null) {
			allObservations.addAll(processObservationBundle(observationBundle));
			observationBundle = (observationBundle.getLink(Bundle.LINK_NEXT) != null) ? getClient().loadPage().next(observationBundle).execute() : null;
		}
		
		return allObservations;
	}

	private List<Observation> processObservationBundle(Bundle observationBundle) {
		if (!observationBundle.hasTotal() || observationBundle.getTotal() > 0) {
			return observationBundle.getEntry().stream().map(bundleEntryComponent -> (Observation) bundleEntryComponent.getResource()).collect(Collectors.toList());
		}
		
		return new ArrayList<>();
	}
	
	@Override
	public IIdType createUpdatePatient(Patient patient) throws FHIRException {
		Patient found = findPatientById(patient.getIdElement().getIdPart());
		if (found != null) {
			MethodOutcome outcome = getClient().update().resource(patient).execute();
			return outcome.getResource().getIdElement();
		}
		
		MethodOutcome outcome = getClient().create().resource(patient).execute();
		return outcome.getResource().getIdElement();
	}

	@Override
	public IIdType createUpdateObservation(Observation observation) throws FHIRException {
		Observation found = findObservationById(observation.getIdElement().getIdPart());
		if (found != null) {
			MethodOutcome outcome = getClient().update().resource(observation).execute();
			return outcome.getResource().getIdElement();
		}
		
		MethodOutcome outcome = getClient().create().resource(observation).execute();
		return outcome.getResource().getIdElement();
	}

}
