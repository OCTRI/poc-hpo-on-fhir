package org.octri.hpoonfhir.service;

import org.octri.hpoonfhir.config.FhirConfig;

public class FhirServiceBuilder {
	
	public static final String STU3_VERSION = "STU3";
	public static final String R4_VERSION = "R4";
	public static final String R5_VERSION = "R5";
	
	public static FhirService build(FhirConfig config) {
		if (config.getVersion().equals(STU3_VERSION)) {
			return new Stu3FhirService(config.getName(), config.getUrl());
		} else if (config.getVersion().equals(R4_VERSION)) {
			return new R4FhirService(config.getName(), config.getUrl());
		} else if (config.getVersion().equals(R5_VERSION)) {
			return new R5FhirService(config.getName(), config.getUrl());
		}
		
		throw new IllegalArgumentException("The FHIR version " + config.getVersion() + " is not supported.");
	}

}
