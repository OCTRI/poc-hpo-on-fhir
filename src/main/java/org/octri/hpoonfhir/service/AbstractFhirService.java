package org.octri.hpoonfhir.service;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;

/**
 * The extending STU2 and STU3 services set their own context and implement interface methods, returning STU3 entities.
 * 
 * @author yateam
 */
public abstract class AbstractFhirService implements FhirService {
	
	private String serviceName;
	
	public abstract FhirContext getFhirContext();
	
	public AbstractFhirService(String serviceName) {
		this.serviceName = serviceName;
	}
	
	@Override
	public String getServiceName() {
		return serviceName;
	}
	
	/**
	 * This provides a shortcut to seeing the json for debugging/logging purposes
	 * @param resource
	 * @return the resource as json
	 */
	protected String resourceAsString(IBaseResource resource) {
		return getFhirContext().newJsonParser().encodeResourceToString(resource);
	}
	
}
