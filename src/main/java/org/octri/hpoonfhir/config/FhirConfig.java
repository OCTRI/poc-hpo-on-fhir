package org.octri.hpoonfhir.config;

import org.octri.hpoonfhir.service.Stu2FhirService;
import org.octri.hpoonfhir.service.Stu3FhirService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirConfig {
	
	@Bean
	public Stu3FhirService r3FhirService() {
		return new Stu3FhirService("https://r3.smarthealthit.org");
	}

	@Bean
	public Stu2FhirService epicFhirService() {
		return new Stu2FhirService("https://open-ic.epic.com/FHIR/api/FHIR/DSTU2");
	}

}
