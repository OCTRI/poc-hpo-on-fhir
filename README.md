# Proof of Concept: HPO on FHIR

This simple application can search for patients in the hardcoded sandbox and display LOINC Codes within observations along with the corresponding HPO Term if one exists. You can also see all the LOINC Codes that have been annotated and the raw annotations for each.

Running the app will require the following:

- Locally install the phenol library which is a dependency of the fhir2hpo library: https://github.com/monarch-initiative/phenol
- Locally install the fhir2hpo library: https://github.com/OCTRI/fhir2hpo

I am unable to get this application running as a jar. However, from the command line you can type 'mvn spring-boot:run' and then navigate to http://localhost:8080. Alternatively, this can be run in Eclipse.