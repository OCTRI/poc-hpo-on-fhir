package org.octri.hpoonfhir.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

/**
 * The extending STU2 and STU3 services set their own context and implement
 * interface methods, returning STU3 entities.
 * 
 * @author yateam
 */
public abstract class AbstractFhirService implements FhirService {

	private String serviceName;
	private final IGenericClient client;

	public abstract FhirContext getFhirContext();

	public AbstractFhirService(String serviceName, String url) {
		this.serviceName = serviceName;
		getFhirContext().getRestfulClientFactory().setSocketTimeout(30 * 1000); // Extend the timeout
		client = getFhirContext().newRestfulGenericClient(url);
	}

	@Override
	public String getServiceName() {
		return serviceName;
	}

	public IGenericClient getClient() {
		return client;
	}

	/**
	 * This provides a shortcut to seeing the json for debugging/logging purposes
	 * 
	 * @param resource
	 * @return the resource as json
	 */
	protected String resourceAsString(IBaseResource resource) {
		return client.getFhirContext().newJsonParser().encodeResourceToString(resource);
	}

	public <T extends IAnyResource> T getResourceById(Class<T> clazz, String id) {
		try {
			// The HAPI Fhir server can't parse integer ids when passed as a string
			return client.read().resource(clazz).withId(Long.parseLong(id)).execute();
		} catch (NumberFormatException e) {
			return client.read().resource(clazz).withId(id).execute();
		} catch (ResourceNotFoundException e) {
			return null;
		}
	}

	public void deleteResourceById(IdType id) {
		client.delete().resourceById(id).execute();
	}
	
	@Override
	public List<Patient> processPatientBundle(Bundle patientBundle) {
		if (!patientBundle.hasTotal() || patientBundle.getTotal() > 0) {
			return patientBundle.getEntry().stream().map(bundleEntryComponent -> (Patient) bundleEntryComponent.getResource()).collect(Collectors.toList());
		}
		
		return new ArrayList<>();
	}

	
}
