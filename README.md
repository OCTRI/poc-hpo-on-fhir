# Proof of Concept: HPO on FHIR

This simple application can search for patients in a hardcoded sandbox and display LOINC Codes within observations along with the corresponding HPO Term if one exists. You can also see all the LOINC Codes that have been annotated and the raw annotations for each.

Running the app will require the following:

- Install Maven for building code. Run ```mvn clean install` on the following libraries to get them into your local repository. Run these in order:
- The phenol library: https://github.com/monarch-initiative/phenol
- The fhir2hpo library: https://github.com/OCTRI/fhir2hpo
- This application

You can run this several ways.

As a jar:

```java -jar target/hpoOnFhir.jar```

As a spring-boot app:

```mvn spring-boot:run```

You can also set up the Eclipse Spring Tool Suite with all the projects and run using the Boot dashboard. This is best for development as it will track changes in the underlying library so you don't have to rerun maven each time.

Note: Most of the patients in the R3 sandbox do not have observations with Interpretations or Reference Ranges, meaning they cannot be converted. Search for Frank Taylor. He's an exception.
