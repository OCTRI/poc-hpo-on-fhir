package org.octri.hpoonfhir.config;

import org.octri.hpoonfhir.service.FhirService;
import org.octri.hpoonfhir.service.FhirServiceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "fhir-server-configuration")
@Configuration
public class FhirConfig {

	private String name;
	private String url;
	private String version;
	private String authorize;
	private String token;
	private String redirect;
	private String clientId;
	// While this class is not Serializable, declaring this transient will protect against accidental exposure if it becomes so.
	private transient String clientSecret;

	public FhirConfig() {

	}

	public FhirConfig(String name, String url, String version, String authorize, String token, String redirect, String clientId, String clientSecret) {
		this.name = name;
		this.url = url;
		this.version = version;
		this.authorize = authorize;
		this.token = token;
		this.redirect = redirect;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	
	public String getAuthorize() {
		return authorize;
	}

	
	public void setAuthorize(String authorize) {
		this.authorize = authorize;
	}

	
	public String getToken() {
		return token;
	}

	
	public void setToken(String token) {
		this.token = token;
	}

	
	public String getRedirect() {
		return redirect;
	}

	
	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

	
	public String getClientId() {
		return clientId;
	}

	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	
	public String getClientSecret() {
		return clientSecret;
	}

	
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	/**
	 * Construct the FhirService from configuration
	 * @return
	 */
	@Bean
	public FhirService fhirService() {
		return FhirServiceBuilder.build(this);
	}

}
