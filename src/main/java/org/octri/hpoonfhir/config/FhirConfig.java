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

	public FhirConfig() {

	}

	public FhirConfig(String name, String url, String version) {
		this.name = name;
		this.url = url;
		this.version = version;
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

	/**
	 * Construct the FhirService from configuration
	 * @return
	 */
	@Bean
	public FhirService fhirService() {
		return FhirServiceBuilder.build(this);
	}

}
