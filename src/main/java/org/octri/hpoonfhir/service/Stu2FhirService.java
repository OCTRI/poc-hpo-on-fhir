package org.octri.hpoonfhir.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.convertors.NullVersionConverterAdvisor30;
import org.hl7.fhir.convertors.VersionConvertor_10_30;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.exceptions.FHIRException;
import org.octri.hpoonfhir.config.FhirConfig;
import org.octri.hpoonfhir.controller.exception.Stu3ConversionException;

import ca.uhn.fhir.context.FhirContext;


/**
 * The implementation of the STU2 FHIR service. FHIR communication uses STU2 and results are converted to STU3.
 * 
 * @author yateam
 *
 */
public class Stu2FhirService extends AbstractFhirService {
	
	private static final Logger logger = LogManager.getLogger();
	private static final FhirContext ctx = FhirContext.forDstu2Hl7Org();
	private final VersionConvertor_10_30 converter = new VersionConvertor_10_30(new NullVersionConverterAdvisor30());

	public Stu2FhirService(FhirConfig config) {
		super(config);
	}

	@Override
	public FhirContext getFhirContext() {
		return ctx;
	}
	
	@Override
	public Patient findPatientById(String token, String id) {
		org.hl7.fhir.instance.model.Patient stu2Patient = getClient(token).read().resource(org.hl7.fhir.instance.model.Patient.class).withId(id).execute();
		try {
			return converter.convertPatient(stu2Patient);
		} catch (FHIRException e) {
			logger.error("Error converting from DSTU2 to DSTU3");
			throw new Stu3ConversionException();
		}
	}

	@Override
	public List<Patient> findPatientsByFullName(String token, String firstName, String lastName) {
		org.hl7.fhir.instance.model.Bundle patientBundle = getClient(token).search().forResource(org.hl7.fhir.instance.model.Patient.class).where(Patient.FAMILY.matches().value(lastName)).and(Patient.GIVEN.matches().value(firstName)).returnBundle(org.hl7.fhir.instance.model.Bundle.class).execute();
		try {
			return processPatientBundle(patientBundle);
		} catch (FHIRException e) {
			logger.error("Error converting from DSTU2 to DSTU3");
			throw new Stu3ConversionException();
		}
	}
	
	@Override
	public List<Observation> findObservationsForPatient(String token, String patientId) {
		List<Observation> allObservations = new ArrayList<>();
		// Epic sandbox query will fail if category is not provided
		org.hl7.fhir.instance.model.Bundle observationBundle = getClient(token).search()
				.byUrl("Observation?patient=" + patientId + "&category=vital-signs,laboratory")
				.returnBundle(org.hl7.fhir.instance.model.Bundle.class).execute();
		
		try {

			while (observationBundle != null) {
				allObservations.addAll(processObservationBundle(observationBundle));
				observationBundle = (observationBundle.getLink(Bundle.LINK_NEXT) != null) ? getClient(token).loadPage().next(observationBundle).execute() : null;
			}
			
			return allObservations;

		} catch (FHIRException e) {
			logger.error("Error converting from DSTU2 to DSTU3");
			throw new Stu3ConversionException();
		}
	}

	@Override
	public Observation findObservationById(String token, String id) {
		org.hl7.fhir.instance.model.Observation stu2Observation = getClient(token).read().resource(org.hl7.fhir.instance.model.Observation.class).withId(id).execute();
		try {
			return (Observation) converter.convertObservation(stu2Observation);
		} catch (FHIRException e) {
			logger.error("Error converting from DSTU2 to DSTU3");
			throw new Stu3ConversionException();
		}
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

	private List<Condition> processConditionBundle(org.hl7.fhir.instance.model.Bundle observationBundle) throws FHIRException {
		List<Condition> stu3Observations = new ArrayList<>();
		if (!observationBundle.hasTotal() || observationBundle.getTotal() > 0) {
			for (org.hl7.fhir.instance.model.Bundle.BundleEntryComponent bundleEntryComponent: observationBundle.getEntry()) {
				org.hl7.fhir.instance.model.Condition stu2Observation = (org.hl7.fhir.instance.model.Condition) bundleEntryComponent.getResource();
				Condition stu3Observation = (Condition) converter.convertCondition(stu2Observation);
				stu3Observations.add(stu3Observation);
			}
		}
		
		return stu3Observations;		
	}

	@Override
	public List<Condition> findConditionsForPatient(String token, String patientId) {
		List<Condition> allConditions = new ArrayList<>();
		org.hl7.fhir.instance.model.Bundle conditionBundle = getClient(token).search()
				.byUrl("Condition?patient=" + patientId)
				.returnBundle(org.hl7.fhir.instance.model.Bundle.class).execute();
		
		try {

			while (conditionBundle != null) {
				allConditions.addAll(processConditionBundle(conditionBundle));
				conditionBundle = (conditionBundle.getLink(Bundle.LINK_NEXT) != null) ? getClient(token).loadPage().next(conditionBundle).execute() : null;
			}
			
			return allConditions;

		} catch (FHIRException e) {
			logger.error("Error converting from DSTU2 to DSTU3");
			throw new Stu3ConversionException();
		}
	}

	@Override
	public List<Condition> findConditionsByCode(String token, String code) {
		//TODO: Not a sustainable search. Just grab the first bundle
		List<Condition> allConditions = new ArrayList<>();
		org.hl7.fhir.instance.model.Bundle conditionBundle = getClient(token).search()
				.byUrl("Condition?code=" + code)
				.returnBundle(org.hl7.fhir.instance.model.Bundle.class).execute();
		
		try {

			while (conditionBundle != null) {
				allConditions.addAll(processConditionBundle(conditionBundle));
				//conditionBundle = (conditionBundle.getLink(Bundle.LINK_NEXT) != null) ? getClient(token).loadPage().next(conditionBundle).execute() : null;
				conditionBundle = null;
			}
			
			return allConditions;

		} catch (FHIRException e) {
			logger.error("Error converting from DSTU2 to DSTU3");
			throw new Stu3ConversionException();
		}
	}

	@Override
	public Condition findConditionById(String token, String id) {
		org.hl7.fhir.instance.model.Condition stu2Condition = getClient(token).read().resource(org.hl7.fhir.instance.model.Condition.class).withId(id).execute();
		try {
			return (Condition) converter.convertCondition(stu2Condition);
		} catch (FHIRException e) {
			logger.error("Error converting from DSTU2 to DSTU3");
			throw new Stu3ConversionException();
		}
	}

}
