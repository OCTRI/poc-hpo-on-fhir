# Proof of Concept: HPO on FHIR

This web application demonstrates the use of the [fhir2hpo](https://github.com/OCTRI/fhir2hpo) Java library for converting FHIR-encoded laboratory results with LOINC information to terms in the [Human Phenotype Ontology](https://hpo.jax.org/app/). Users can search for patients in an unauthenticated FHIR sandbox and display the phenotypes derived from converting Observation LOINCs.

This application can connect to STU2 and STU3 servers, and uses the HAPI Converter library to format responses as STU3 for use in the fhir2hpo library.

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
git checkout v1.0.0
mvn install
```

This checks out and builds Release 1.0.0 of fhir2hpo. Both libraries are now installed in your local Maven repository.

Finally, clone this library and run `mvn install` to build the jar.

## Running the Application

You can run this application in a few different ways. Run as a jar:

```
java -jar target/hpoOnFhir.jar
```

Or as a spring-boot app:

```
mvn spring-boot:run
```

You can also set up the Eclipse Spring Tool Suite with all the projects and run using the Boot dashboard. This is best for development as it will track changes in the underlying library so you don't have to rerun maven each time.

## Changing Sandboxes

The application is configured by default to work with the R3 SmartHealth IT sandbox, but is configured to work with the HSPC, Open Epic, and Epic App Orchard servers. It is designed with SMART principles in mind and should work with any other open FHIR servers.  To use one of the other sandboxes run the application with the profile name of the sandbox as an argument. For example:

```
java -jar target/hpoOnFhir.jar --spring.profiles.active=openepic
```

To modify or add a sandbox, open the file [application.yml](src/main/resources/application.yml) and modify an existing section or create a new profile:

```
---
spring:
  profiles: mysandbox
fhir-server-configuration:
  name: My Sandbox
  url: https://fhir.mysandbox.org
  version: STU3
```

Change these properties and restart the application to connect to a different sandbox.

These properties can also be overwritten either using environment variables or passed on the command line like the profile. For example to replace the URL for a particular profile you can do one or another of the following:

``` with command line arguments
java -jar target/hpoOnFhir.jar --spring.profiles.active=openepic \
    --fhir-server-configuration.url=https://a-new-url.org
```

``` with environment variables
export SPRING_PROFILES_ACTIVE=openepic
export FHIRSERVERCONFIGURATION_URL=https://a-new-url.org

java -jar target/hpoOnFhir.jar
```
Other servers we have tested with are:

- http://hapi.fhir.org/baseDstu3 (The HAPI FHIR STU3 server)
- https://api-stu3.hspconsortium.org/STU301withSynthea/open (The HSPC STU3 server)
- https://r2.smarthealthit.org (The SmartHealth IT STU2 server)
