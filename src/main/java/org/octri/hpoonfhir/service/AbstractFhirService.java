package org.octri.hpoonfhir.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * The extending STU2 and STU3 services set their own context and implement interface methods, returning STU3 entities.
 * 
 * @author yateam
 */
public abstract class AbstractFhirService implements FhirService {
	
	private final IGenericClient client;
	
	public abstract FhirContext getFhirContext();
	
	public AbstractFhirService(String url) {
		getFhirContext().getRestfulClientFactory().setSocketTimeout(30 * 1000); // Extend the timeout
		client = getFhirContext().newRestfulGenericClient(url);
	}
	
	public IGenericClient getClient() {
		return client;
	}
	
}
