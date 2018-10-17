package org.octri.hpoonfhir.service;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;

/**
 * The extending STU2 and STU3 services set their own context and implement interface methods, returning STU3 entities.
 * 
 * @author yateam
 */
public abstract class AbstractFhirService implements FhirService {
	
	private String url;
	private String serviceName;
	
	public abstract FhirContext getFhirContext();
	
	public AbstractFhirService(String serviceName, String url) {
		this.url = url;
		this.serviceName = serviceName;
		getFhirContext().getRestfulClientFactory().setSocketTimeout(30 * 1000); // Extend the timeout
	}
	
	@Override
	public String getServiceName() {
		return serviceName;
	}
	
	public IGenericClient getClient(String token) {
		IGenericClient client = getFhirContext().newRestfulGenericClient(url);
		BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token);
		client.registerInterceptor(authInterceptor);
		return client;
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
