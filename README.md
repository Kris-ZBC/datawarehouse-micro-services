# Author
Kris Kristensen - instructor SOP Ringsted

# API-Service
API-Service is the backend of a larger project called SOPInfo. SOPInfo aims at providing both appretencies and instructors guidance and information about any given apprentice about his/hers check-in time. The API-Service serves as the backend system for the app and web frontends, comprised of small dockerized microservices within the same docker network. 

This project is designed based on the principles of DDD - Domain Driven Development and CA - Clean Architecture including the more wellknown onion architecture paradigm. There are exceptions to this rule. Please consolidate the Microservices paragraph.

## structural build-up of the microservice architecture

### Server microservices - DB aware
To better understand the architectural structure of the server-microservices, please refer to the plantuml: infra/architecture/server-Microservice architecture.puml diagram.

### SAGA microservices


The diagram shows the five key layers of bc-consent from top to bottom:

Interface web — the REST controller receiving inbound HTTP calls
Application layer — the ConsentDirectory interface and ConsentApplicationService that orchestrate use cases
Domain layer — the pure domain model (Consent, ConsentStatement, value objects), domain service, and outbound port interfaces
Interface adapters — JPA persistence, security config, and external systems (MariaDB, Flyway, TLS)
External callers — other BCs and saga handlers communicating via mTLS

The same structure applies to all other BCs — just swap the names.

## Design architectural structure
The structure of developing this API backend proprises of the following gitlab philosophy:
 - Main branch (protected)
 - two long-lived branches (protected)
 - Any number of feature-branches created by the developers, and pushed to their respective long-lived branch. The developers must create a merge request in gitlab, in order to push their changes to their assigned feature branch.

Each long-lived branch has a maintainer role attached to it, and only the maintainer can create a merge request to the main branch.

The assigned maintainers of the long-lived branches, must make sure that the main branch is kept abreast frequently to avoid lacking too much behind commmits to the main branch. The rule is simple at least once a week (friday) the maintainers creates a merge request to push their respective long lived branch to the main branch. Once both long-lived branches are pushed, each branch creates a merge request to update their respective long lived branch from the main branch. This way ensures taht both long lived branches a kept abreast with the latest changers at least once a week.

This project provides the code blocks in Java using java Springboot. The code ethics should follow the ccode conventions provided by Oracle Inc.:
https://www.oracle.com/technetwork/java/codeconventions-150003.pdf

The project is deployed using docker and CI/CD pipeline strategy. See below for further instructions

This project is created by the SOP Ringsted/Denmark by the appretencies present at that time


## Microservices
All the bounded contexts, abbrevated BC in the code are deployed as microservices via docker using CI/CD pipeline from gitlab. Gitlab is a locally installed instance within the SOP network reachable at https://gitlab.sop.local

The CI/CD pipeline will be responsible for the orchestration of the test, build and staging deploy of the microservices using docker compose. 

The microservices will be self contained and in its own atomic state. The microservice will expose required endpoints for specifically designed handlers to use in order to accomplish CRUD actions against the BC. All endpoints will be TLS protected, except for a few exceptions. The BC-Organisation, BC-Institution and BC-SOP are regarded as not being a vital of the SOPinfo system, and is only available in read-only mode. Data for these endpoints are seeded during setup, and are not supposed to be affected by the assigned BC from any endpoints, hence keeps its read-only tranactional state.

All the atomic state bounded contexts will be reachable using only the /internal/** endpoint. That endpoint will be TLS protected. They will all be rachable through https://<BC-NAME>.sop.local. They will all use ports in the range of 9443 - 9499. Their application.properties file will reveal their respective port number. The port number will be a CI friendly attribute, so it can be controlled by the pipeline should it be required. These BC's will all need server certificates, since they cannot ever initiate communication with other BC's. The PRIMARY KEY for Entity for the the respective BC should always be a UUID. However, in the database it should always be stored as a binary. The binary is the preferred modern way to store an UUID, and jps converts from/to binary to GUID with the below setting in application.properties:

## prefer BINARY(16) for UUID storage
spring.jpa.properties.hibernate.type.preferred_uuid_jdbc_type=BINARY

In the Entity class, define the PRIMARY KEY like shown below:

## Entity class using BINARY as PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "OrderId", columnDefinition = "BINARY(16)")
    private java.util.UUID orderId;



## Port assignments SERVERS
bc-apprentice            -> 9443
bc-auditlog              -> 9444 
bc-consent               -> 9445
bc-education-line        -> 9446
bc-institution           -> 9447
bc-instructor            -> 9448
bc-message               -> 9449
bc-notification          -> 9450
bc-organisation          -> 9451 
bc-person                -> 9452
bc-sop                   -> 9453
bc-work-hour             -> 9454
bc-login                 -> 9455
bc-message-person        -> 9456
bc-sop-education         -> 9457
bc-sop-instructor        -> 9458
bc-education             -> 9459
bc-person-notification   -> 9460
bc-anonymize             -> 9499


Handlers and SAGA BC's will be reachable using only the /handlers/** endpoint. That endpoint will be TLS protected. They will be reachable through https://<BC-NAME>.sop.local. They will all use ports in the range of 8443 - 8499. Their application.properties will reveal their respective port number. The port number will be a CI friendly attribute, so it can be controlled by the pipeline should it be required. Some of the handlers or SAGA will need both a client and a server certificate. They will need client certificates to communicate with any atomic state BC, but sometimes also serve context to other handlers.  In that case certificates both as a client and server must be generated. See certificate generation below.

## Port assignments HANDLERS/SAGAs
bc-consent-saga           -> 8343
bc-login-saga             -> 8344
bc-registration-saga      -> 8345
bc-education-line-saga    -> 8346
bc-education-saga         -> 8347
bc-anonymize-saga         -> 8348
bc-message-saga           -> 8349


bc-auth_handler           -> 8443
bc-check_in_handler       -> 8444
bc-communication_handler  -> 8445
bc-organisation_handler   -> 8446
bc-sop_handler            -> 8447
bc-user_handler           -> 8448
bc-anonymize-handler      -> 8499


## Certificate Generation
Generating certificates is a request send to the infrastructure team in Ringsted SOP. The infrastructure team will need to know if you requesting a server, a client or both a server and a client certificate, a socalled mutual certificate. The developer should request certificates only by advising a *.cnf file. The *.cnf file is the building blocks for the certificate. Once done infrastructure will make the certificate available at https://172.20.1.16/certificate. This URL is protected by a username/pwd. The username/pwd is provided by the infrastructure team. 
#### NOTE! - If the developer wishes to run all the microservices locally, it is important! that he/she maps the URL to the drive letter P: in Windows. If Y: is not used, it will affect all the application.properties files for every microservice!.  

### Examples of *.cnf files

#### Server certiifcate cnf file:

<microservice>-csr.cnf:
[ req ]
prompt = no
default_md = sha256
distinguished_name = dn
req_extensions = req_ext

[ dn ]
CN = instructor <-- must be the microservice name

[ req_ext ]
subjectAltName = @alt_names
extendedKeyUsage = serverAuth <-- <VERY IMPORTANT!>
keyUsage = digitalSignature, keyEncipherment

[ alt_names ]
DNS.1 = bc-instructor.sop.local <-- <VERY IMPORTANT!>

#### END cnf

#### Client certificate cnf file:
[ req ]
prompt = no
default_md = sha256
distinguished_name = dn
req_extensions = req_ext

[ dn ]
CN = registration-saga <-- <VERY> IMPORTANT!

[ req_ext ]
extendedKeyUsage = clientAuth <-- <VERY> IMPORTANT!
keyUsage = digitalSignature


#### END cnf

Sometimes the certificate must be a mutual certificate. This is required if the BC needs to act both as a server and a client. One example would be a saga that is called from a handler or a gateway. The mutual certificate looks a lot like the client certificate, but with vital differences.

#### Mutual certificate cnf file:

[ req ]
prompt = no
default_md = sha256
distinguished_name = dn
req_extensions = req_ext

[ dn ]
CN = instructor-handler

[ req_ext ]
extendedKeyUsage = serverAuth, clientAuth <-- <VERY>> IMPORTANT!
keyUsage = digitalSignature, keyEncipherment, dataEncipherment <-- <VERY>> IMPORTANT!

[ alt_names ]
DNS.1 = instructor-handler.sop.local <-- <VERY IMPORTANT!>

#### END cnf

once the certicates are generated, they must be added to the respective application.properties file. Below is an example of a client and a server application.properties file.

#### Server application.properties file

server.port={SERVER_PORT:9443}
spring.application.name=instructor-service
server.address=0.0.0.0 <-- required for local testing

# DB container/schema
spring.datasource.url=jdbc:postgresql://localhost:5433/registrants
spring.datasource.username=instructor
spring.datasource.password=instructor

##### Tell Hibernate/Flyway to use instructor schema
spring.jpa.properties.hibernate.default_schema=instructor
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false

spring.flyway.schemas=instructor
spring.flyway.default-schema=instructor

##### mTLS allowlist for /internal/**
internal.mtls.allowed-callers=registration-saga <-- <VERY> IMPORTANT! This tells the server which handlers/sagas it is allowed to interact with.

##### --- TLS / mTLS (server side) ---
tls.dir=${TLS_DIR:Y:/} <-- Y is a Mapped network drive
keystore.password=${TLS_KEYSTORE_PASSWORD:4Ndet0wn!} <-- pwd provided by the infrastructure team. NOTE! only required for local tests
truststore.password=${TLS_TRUSTSTORE_PASSWORD:4Ndet0wn!} <-- pwd provided by the infrastructure team. NOTE! only required for local tests

##### SSL bundle
spring.ssl.bundle.jks.instructor.key.alias=1

spring.ssl.bundle.jks.instructor.keystore.location=file:${tls.dir}/instructor.p12 <-- Generated certificate
spring.ssl.bundle.jks.instructor.keystore.password=${keystore.password}
spring.ssl.bundle.jks.instructor.keystore.type=PKCS12

spring.ssl.bundle.jks.instructor.truststore.location=file:${tls.dir}/internal-ca-trust.p12 <-- Root certificate
spring.ssl.bundle.jks.instructor.truststore.password=${truststore.password}
spring.ssl.bundle.jks.instructor.truststore.type=PKCS12

##### use ssl bundle
server.ssl.enabled=true
server.ssl.bundle=instructor
server.ssl.client-auth=need
server.ssl.enabled-protocols=TLSv1.2,TLSv1.3

##### log4j
logging.level.root=INFO 
logging.level.local.lyngberg=DEBUG 
logging.pattern.console=%d{HH:mm:ss.SSS} %-5level %logger{36} - %msg%n

##### Actuator
management.endpoints.web.exposure.include=health,info
management.endpoint.health.probes.enabled=true
management.health.livenessstate.enabled=true
management.health.readinessstate.enabled=true

##### i18n
spring.messages.basename=i18n/messages
spring.messages.encoding=UTF-8
spring.mvc.problemdetails.enabled=true

#### Client application.properties file:
server.port=${SERVER_PORT:8443}
spring.application.name=registration-service

##### ---------- Downstream base URLs ----------
instructor.base-url=${INSTRUCTOR_BASE_URL:https://bc-instructor.sop.local:9443}

##### Actuator
management.endpoints.web.exposure.include=health,info
management.endpoint.health.probes.enabled=true
management.health.livenessstate.enabled=true
management.health.readinessstate.enabled=true

##### i18n
spring.messages.basename=i18n/messages
spring.messages.encoding=UTF-8
spring.mvc.problemdetails.enabled=true

##### --- TLS dirs (kan overrides med env vars) ---
tls.dir=${TLS_DIR:Y:/}
keystore.password=${TLS_KEYSTORE_PASSWORD:4Ndet0wn!}
truststore.password=${TLS_TRUSTSTORE_PASSWORD:4Ndet0wn!}

##### --- mTLS client config for calling Instructor (/internal/**) ---
instructor.mtls.key-store=file:${tls.dir}/registration-saga.p12
instructor.mtls.key-store-type=PKCS12
instructor.mtls.key-store-password=${keystore.password}
instructor.mtls.key-alias=1

instructor.mtls.trust-store=file:${tls.dir}/internal-ca-trust.p12
instructor.mtls.trust-store-type=PKCS12
instructor.mtls.trust-store-password=${truststore.password}

#### log4j settings
logging.level.org.springframework.boot.ssl=DEBUG
logging.level.org.springframework.boot.web.server=DEBUG
logging.level.org.apache.tomcat.util.net=DEBUG


#### i18n - internationalization
The project utilizes i18n resource bundles. Therefore every BC must adhere to the <RFC 3066> standard for commicating erros arising from the BC. The i18n message keys must be stored in a <microservicename>-messages.properties file for the default and a <microservicename>-messages_<language>.properties file for the <language>. Example for Danish messages keys for BC instructor: instructor-messages_da.properties, 
and default for same instructor-messages.properties

The default keys file will contain the english version of the message key value.

#### An example of localized key (shared-messages_da.properties):
##### shared keys
uuid.required =UUID nøgle er krævet
uuid.invalid =UUID nøgle er ugyldig

#### An example of default key (shared-messages.properties):
##### shared keys
uuid.required =UUID key required
uuid.invalid =UUID key is invalid

## Infrastrucure as a service
The entire test, build and deploy to gitlab registry is maintained using gitlab-ci pipeline. The pipeline uses a number of scripts to produce the correct and dynamically generated artifacts. The infrastructure scripts are located in:
<PROJECT_ROOT>/infra/scrips

There are Three types of scrips:
  1. *.sh   -> scripts used by the pipeline
  2. *.ps1  -> Powershell wrapping of the same *.sh script
  3. *.py   -> Python scripts for generating and injecting code directly in the source (Requires Python 3)

  Currently IAAS contains of the floowing scripts:

  #### sh scripts ####
  gen-compose.sh                  -> called by the pipeline for generaing the docker-compose.yml file
  gen-db-provision.sh             -> Called by the pipeline for generating the DBMS
  gen-dockerfiles.sh              -> Called by the pipeline for generating for Dockerfile per microservice dynamically
  gen-manifest.sh                 -> Called by the pipeline for generating a manifest.yml file explaining the total number of microservices generated and their FQDN/names:tag
  run-provision.sh                -> Used in deprecated deploy phase
  tls-entrypoint.sh               -> Used by gen-dockerfiles.sh

  #### py scripts ####
  fix-modifying.py                -> Run once (or as --dry-run) to verify/correct that @Modifying for update methods use 'clearAutomatically = true' (Best practice)
  gen-ddl.py                      -> Runs through every entity and generates (or as --dry-run) migration files for every server-microservice 

  #### ps1 ####
  clean-artifacts.ps1             -> Clean up of mistakenly auto generated files like docker-compose.yml etc.
  prune-local-gone-branches.ps1   -> prunes all local branches that have status 'gone' on the remote side



## Deployment strategy
all microservices are deployed using docker. Every microservice is responsible for its own dockerfile. Docker compose and gitlab CI files are located at the root of the project, and acts as orchestrators for the respective area. docker compose orchestrates all of the microservices and gitlab CI orchestrates the pipeline using docker.

### Docker per microservice. 
Docker per microservice is contained in its respective dockerfile. The dockerfile will make sure that the microservice is runnable once the stand-alone jar file has been compiled by maven. Verify that the <bc>-impl/pom.xml contains at least the following entry:

### Snippet pom.xml

<properties>
    <start-class><FQDN-part>.microservices.<bc-name>.<BC-name>StartApplication</start-class>
    <spring-boot.run.skip>false</spring-boot.run.skip>
  </properties>

  <build>
    <plugins>
      <!-- makes an executable Spring Boot jar -->
    <plugin>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-maven-plugin</artifactId>
      <executions>
        <execution>
          <goals>
            <goal>repackage</goal> <-- <VERY> IMPORTANT!
          </goals>
        </execution>
      </executions>
      <!-- (Optional) if auto-detection fails, use the start-class property -->
        <configuration>
          <mainClass>${start-class}</mainClass>
        </configuration>
    </plugin>
    </plugins>
  </build>

