package org.octri.hpoonfhir.service;

import org.octri.hpoonfhir.config.FhirConfig;

public class FhirServiceBuilder {
	
	public static final String STU2_VERSION = "STU2";
	public static final String STU3_VERSION = "STU3";
	
	public static FhirService build(FhirConfig config) {
		if (config.getVersion().equals(STU2_VERSION)) {
			return new Stu2FhirService(config.getName(), config.getUrl());
		} else if (config.getVersion().equals(STU3_VERSION)) {
			return new Stu3FhirService(config.getName(), config.getUrl());
		}
		
		throw new IllegalArgumentException("The FHIR version " + config.getVersion() + " is not supported.");
	}

}
