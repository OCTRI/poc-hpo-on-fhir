# Proof of Concept: HPO on FHIR

## Description

This web application demonstrates the utility of the [fhir2hpo](https://github.com/OCTRI/fhir2hpo) Java library for automatically converting FHIR-encoded laboratory results with LOINC information to terms in the [Human Phenotype Ontology](https://hpo.jax.org/app/). Users can view synthetic patients in the unauthenticated FHIR sandbox and display the phenotypes derived from converting Observation LOINCs.

Users act as curators, reviewing the automatically generated phenotypes to determine which ones should be added to the clinical record. The curator can then select phenotypes to push back up to the FHIR server conforming to the [Phenopackets IG](https://github.com/phenopackets/core-ig).

This application can connect to STU3, R4, and R5 servers, and uses the HAPI Converter library to format responses as R5 for use in the fhir2hpo library.

## Setup

Compiling this library requires installation of [Maven](http://maven.apache.org/install.html) and [Java 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).

In addition, you need to locally install two libraries that are not yet available on Maven Central. The [monarch-initiative phenol library](https://github.com/monarch-initiative/phenol) defines the domain around the HPO. From the command line:

```
git clone https://github.com/monarch-initiative/phenol.git
cd phenol
git checkout v.1.2.6
mvn install
``` 

This checks out and builds Release 1.2.6 of phenol. 

Next install the fhir2hpo library.

```
git clone https://github.com/OCTRI/fhir2hpo
cd fhir2hpo
git checkout v1.0.5
mvn install
``` 

This checks out and builds Release 1.0.5 of fhir2hpo. Both libraries are now installed in your local Maven repository.

Finally, clone this library and run `mvn install` to build the jar.

## Running the Application

You can run this application in a few different ways. Run as a jar:

```java -jar target/hpoOnFhir.jar```

Or as a spring-boot app:

```mvn spring-boot:run```

You can also set up the Eclipse Spring Tool Suite with all the projects and run using the Boot dashboard. This is best for development as it will track changes in the underlying library so you don't have to rerun Maven each time.

## Changing Sandboxes

The application is configured by default to work with a sandbox set up by Johns Hopkins University that contains a small number of patients and more than 1000 LOINC-encoded observations. It is designed with SMART principles in mind and should work with other open FHIR servers. To modify the sandbox, open the file [application.properties](src/main/resources/application.properties) and look for the following variables:

```
fhir-server-configuration.name=Vulcan Server
fhir-server-configuration.url=http://vulcan.clinicalprofiles.org:8080/fhir
fhir-server-configuration.version=R4
```
Change these properties and restart the application to connect to a different sandbox. Note, however, that the current application lists every patient in the first bundle request on the main page. For most sandboxes, this may not be particularly useful unless those patients happen to have LOINC-encoded observations. To use another server effectively, you may need to create your own resources and modify the code accordingly.

Other servers we have tested with are:

- http://hapi.fhir.org/baseR4/(The HAPI FHIR R4 server)
- HSPC
- SmartHealth IT
- Open Epic (Epic generally does not allow open search for patients without knowing first/last name, so other adjustments may be needed)


