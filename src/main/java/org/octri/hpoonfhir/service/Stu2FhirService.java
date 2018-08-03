package org.octri.hpoonfhir.service;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.convertors.NullVersionConverterAdvisor30;
import org.hl7.fhir.convertors.VersionConvertor_10_30;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.exceptions.FHIRException;

import ca.uhn.fhir.context.FhirContext;


/**
 * The implementation of the STU2 FHIR service. FHIR communication uses STU2 and results are converted to STU3.
 * TODO: Better error handling will be needed, as each FHIR server may respond differently when no results are returned.
 * For example, the Epic Sandbox will not return results based only on a last name. The full name must be provided. 
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
	public List<Patient> findPatientsByFullName(String firstName, String lastName) throws FHIRException {
		org.hl7.fhir.instance.model.Bundle patientBundle = getClient().search().forResource(org.hl7.fhir.instance.model.Patient.class).where(Patient.FAMILY.matches().value(lastName)).and(Patient.GIVEN.matches().value(firstName)).returnBundle(org.hl7.fhir.instance.model.Bundle.class).execute();
		//System.out.println(ctx.newJsonParser().encodeResourceToString(patientBundle));
		return processPatientBundle(patientBundle);
		
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


}
