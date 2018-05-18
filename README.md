# Proof of Concept: HPO on FHIR

This simple application can search for patients in the hardcoded sandbox and display LOINC Codes within
observations along with the corresponding HPO if one exists. You can also see all the LOINC Codes that have been annotated and the raw annotations for each.

Running the app will require some edits:

- The annotations.tsv file is in a hard-coded location in the AnnotationService. Get the latest from the develop branch of https://github.com/TheJacksonLaboratory/loinc2hpoAnnotation
- The hp.obo file is in a hard-coded location in the AnnotationService. Get the latest from master at https://github.com/obophenotype/human-phenotype-ontology
- The maven dependency for loinc2hpo-core must be locally installed. Get it from the octri branch of this fork: https://github.com/aeyates/loinc2hpo
- You may need to locally install the phenol library as well which is a dependency of the core library: https://github.com/monarch-initiative/phenol