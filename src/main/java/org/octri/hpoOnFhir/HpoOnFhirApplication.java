package org.octri.hpoOnFhir;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"org.octri.hpoOnFhir", "org.monarchinitiative.fhir2hpo"})
@EntityScan( basePackages = {"org.octri.hpoOnFhir", "org.monarchinitiative.fhir2hpo"} )
public class HpoOnFhirApplication {

	public static void main(String[] args) {
		SpringApplication.run(HpoOnFhirApplication.class, args);
	}
}
