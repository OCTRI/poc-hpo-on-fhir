package org.octri.hpoonfhir.service;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.convertors.VersionConvertor_40_50;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Observation;
import org.hl7.fhir.r5.model.Patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import io.micrometer.core.instrument.util.StringUtils;

public class R4FhirService extends AbstractFhirService {

	private static final FhirContext ctx = FhirContext.forR4();

	public R4FhirService(String serviceName, String url) {
		super(serviceName, url);
	}

	@Override
	public FhirContext getFhirContext() {
		return ctx;
	}
	
	@Override
	public Patient findPatientById(String id) throws FHIRException {
		org.hl7.fhir.r4.model.Patient patient = getResourceById(org.hl7.fhir.r4.model.Patient.class, id);
		return (Patient) VersionConvertor_40_50.convertResource(patient);
	}

	@Override
	public List<Patient> findPatientsByFullName(String firstName, String lastName) throws FHIRException {
		org.hl7.fhir.r4.model.Bundle patientBundle = getClient().search().forResource(org.hl7.fhir.r4.model.Patient.class).where(Patient.FAMILY.matches().value(lastName)).and(Patient.GIVEN.matches().value(firstName)).returnBundle(org.hl7.fhir.r4.model.Bundle.class).execute();
		return processPatientBundle(patientBundle);		
	}

	@Override
	public Observation findObservationById(String id) throws FHIRException {
		org.hl7.fhir.r4.model.Observation observation = getResourceById(org.hl7.fhir.r4.model.Observation.class, id);
		return (Observation) VersionConvertor_40_50.convertResource(observation);
	}

	@Override
	public List<Observation> findObservationsForPatient(String patientId, String categoryCode) throws FHIRException {
		List<Observation> allObservations = new ArrayList<>();
		String categoryParameter = StringUtils.isBlank(categoryCode) ? "" : "&category=" + categoryCode;
		org.hl7.fhir.r4.model.Bundle observationBundle = getClient().search()
				.byUrl("Observation?patient=" + patientId + categoryParameter)
				.returnBundle(org.hl7.fhir.r4.model.Bundle.class).execute();
		
		
		while (observationBundle != null) {
			allObservations.addAll(processObservationBundle(observationBundle));
			observationBundle = (observationBundle.getLink(Bundle.LINK_NEXT) != null) ? getClient().loadPage().next(observationBundle).execute() : null;
		}
		
		return allObservations;
	}

	@Override
	public IIdType createUpdatePatient(Patient patient) throws FHIRException {
		org.hl7.fhir.r4.model.Patient r4Patient = (org.hl7.fhir.r4.model.Patient) VersionConvertor_40_50.convertResource(patient);
		org.hl7.fhir.r4.model.Patient found = getResourceById(org.hl7.fhir.r4.model.Patient.class, patient.getIdElement().getIdPart());
		return createUpdateResource(r4Patient, found != null);
	}

	@Override
	public IIdType createUpdateObservation(Observation observation) throws FHIRException {
		// NOTE: If the observation has any references, they will only be created properly if the reference is an id and not a whole resource, since the
		// resource is not versioned properly
		org.hl7.fhir.r4.model.Observation r4Observation = (org.hl7.fhir.r4.model.Observation) VersionConvertor_40_50.convertResource(observation);
		org.hl7.fhir.r4.model.Observation found = getResourceById(org.hl7.fhir.r4.model.Observation.class, observation.getIdElement().getIdPart());
		return createUpdateResource(r4Observation, found != null);
	}
	
	private IIdType createUpdateResource(org.hl7.fhir.r4.model.Resource resource, Boolean found) {
		if (found) {
			MethodOutcome outcome = getClient().update().resource(resource).execute();
			return outcome.getResource().getIdElement();
		}
		
		MethodOutcome outcome = getClient().create().resource(resource).execute();
		return outcome.getResource().getIdElement();		
	}

	private List<Patient> processPatientBundle(org.hl7.fhir.r4.model.Bundle patientBundle) throws FHIRException {
		List<Patient> upgradedPatients = new ArrayList<>();
		if (!patientBundle.hasTotal() || patientBundle.getTotal() > 0) {
			for (org.hl7.fhir.r4.model.Bundle.BundleEntryComponent bundleEntryComponent: patientBundle.getEntry()) {
				org.hl7.fhir.r4.model.Patient r4Patient = (org.hl7.fhir.r4.model.Patient) bundleEntryComponent.getResource();
				Patient upgradedPatient = (Patient) VersionConvertor_40_50.convertResource(r4Patient);
				upgradedPatients.add(upgradedPatient);
			}
		}

		return upgradedPatients;		
	}

	private List<Observation> processObservationBundle(org.hl7.fhir.r4.model.Bundle observationBundle) throws FHIRException {
		List<Observation> upgradedObservations = new ArrayList<>();
		if (!observationBundle.hasTotal() || observationBundle.getTotal() > 0) {
			for (org.hl7.fhir.r4.model.Bundle.BundleEntryComponent bundleEntryComponent: observationBundle.getEntry()) {
				org.hl7.fhir.r4.model.Observation r4Observation = (org.hl7.fhir.r4.model.Observation) bundleEntryComponent.getResource();
				Observation upgradedObservation = (Observation) VersionConvertor_40_50.convertResource(r4Observation);
				upgradedObservations.add(upgradedObservation);
			}
		}
		
		return upgradedObservations;		
	}

}
