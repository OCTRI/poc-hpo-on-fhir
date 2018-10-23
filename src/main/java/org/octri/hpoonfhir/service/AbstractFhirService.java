package org.octri.hpoonfhir.service;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.octri.hpoonfhir.config.FhirConfig;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;

/**
 * The extending STU2 and STU3 services set their own context and implement interface methods, returning STU3 entities.
 * 
 * @author yateam
 */
public abstract class AbstractFhirService implements FhirService {

	private final String serviceName;
	private final String url;
	private final String authorizeEndpoint;
	private final String tokenEndpoint;
	private final String redirectUri;
	private final String clientId;
	private final String clientSecret;

	public abstract FhirContext getFhirContext();

	public AbstractFhirService(FhirConfig config) {
		this.serviceName = config.getName();
		this.url = config.getUrl();
		this.authorizeEndpoint = config.getAuthorize();
		this.tokenEndpoint = config.getToken();
		this.redirectUri = config.getRedirect();
		this.clientId = config.getClientId();
		this.clientSecret = config.getClientSecret();
		getFhirContext().getRestfulClientFactory().setSocketTimeout(30 * 1000); // Extend the timeout
	}

	@Override
	public String getServiceName() {
		return serviceName;
	}
	
	@Override
	public String getServiceEndpoint() {
		return url;
	}

	@Override
	public String getAuthorizeEndpoint() {
		return authorizeEndpoint;
	}

	@Override
	public String getTokenEndpoint() {
		return tokenEndpoint;
	}

	@Override
	public String getRedirectUri() {
		return redirectUri;
	}

	@Override
	public String getClientId() {
		return clientId;
	}

	@Override
	public String getClientSecret() {
		return clientSecret;
	}

	/**
	 * Return a client that can send the authentication with requests
	 * 
	 * @param token
	 *            the authentication token
	 * @return
	 */
	public IGenericClient getClient(String token) {
		BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token);
		IGenericClient client = getFhirContext().newRestfulGenericClient(url);
		client.registerInterceptor(authInterceptor);
		return client;
	}

	/**
	 * This provides a shortcut to seeing the json for debugging/logging purposes
	 * 
	 * @param resource
	 * @return the resource as json
	 */
	protected String resourceAsString(IBaseResource resource) {
		return getFhirContext().newJsonParser().encodeResourceToString(resource);
	}

}
