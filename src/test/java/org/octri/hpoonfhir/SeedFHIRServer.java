package org.octri.hpoonfhir;

import static org.junit.Assert.assertEquals;

import org.hl7.fhir.r5.model.Observation;
import org.hl7.fhir.r5.model.Patient;
import org.hl7.fhir.r5.model.Reference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.octri.hpoonfhir.service.FhirService;
import org.octri.hpoonfhir.util.FhirParseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application.properties")
public class SeedFHIRServer {
	
	@Autowired
	FhirService fhirService;
	
	@Test
	public void testAutowiring() {
		Patient patient = fhirService.findPatientById("1505");
		assertEquals("Patient exists", "1505", patient.getIdElement().getIdPart());
	}
	
	@Test 
	public void seedServer() {
    	Patient patient = FhirParseUtils.getPatient("fhir/patient/patient1.json");
		Patient patientWithId = fhirService.createUpdatePatient(patient);
		System.out.println("Patient ID: " + patientWithId.getIdElement().getIdPart());
		
		Observation observation = FhirParseUtils.getObservation("fhir/observation/glucoseHigh.json");
		Reference reference = new Reference(patientWithId);
		observation.setSubject(reference);
		Observation observationWithId = fhirService.createUpdateObservation(observation);
		System.out.println("Observation ID: " + observationWithId.getIdElement().getIdPart());

//		observation = FhirParseUtils.getObservation("fhir/observation/bilirubinPositive.json");
//		observation.setSubject(reference);
//		observationWithId = fhirService.createUpdateObservation(observation);
//		System.out.println("Observation ID: " + observationWithId.getIdElement().getIdPart());
	}

}
