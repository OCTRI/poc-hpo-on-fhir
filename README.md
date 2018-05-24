# Proof of Concept: HPO on FHIR

This simple application can search for patients in the hardcoded sandbox and display LOINC Codes within
observations along with the corresponding HPO if one exists. You can also see all the LOINC Codes that have been annotated and the raw annotations for each.

Running the app will require some edits:

- The maven dependency for fhir2hpo must be locally installed. Get it from here: https://github.com/OCTRI/fhir2hpo
- Locally install the phenol library as well which is a dependency of the fhir2hpo library: https://github.com/monarch-initiative/phenol