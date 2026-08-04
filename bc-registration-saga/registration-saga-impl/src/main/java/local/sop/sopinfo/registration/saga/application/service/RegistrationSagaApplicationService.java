package local.sop.sopinfo.registration.saga.application.service;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import local.sop.sopinfo.registration.saga.application.api.RegistrationDirectory;
import local.sop.sopinfo.registration.saga.application.api.dto.CreateApprenticeRegistrationCmd;
import local.sop.sopinfo.registration.saga.application.api.dto.CreateInstructorRegistrationCmd;
import local.sop.sopinfo.registration.saga.application.api.dto.apprentice.ApprenticeResponse;
import local.sop.sopinfo.registration.saga.application.api.dto.apprentice.CreateApprenticeCmd;
import local.sop.sopinfo.registration.saga.application.api.dto.apprentice.CreatedApprenticeResponse;
import local.sop.sopinfo.registration.saga.application.api.dto.auditlog.AuditlogResponse;
import local.sop.sopinfo.registration.saga.application.api.dto.auditlog.CreateAuditlogCmd;
import local.sop.sopinfo.registration.saga.application.api.dto.consent.ConsentResponse;
import local.sop.sopinfo.registration.saga.application.api.dto.consent.ConsentStatementResponse;
import local.sop.sopinfo.registration.saga.application.api.dto.consent.GrantConsentCmd;
import local.sop.sopinfo.registration.saga.application.api.dto.educationline.EducationLineResponse;
import local.sop.sopinfo.registration.saga.application.api.dto.instructor.CreateInstructorCmd;
import local.sop.sopinfo.registration.saga.application.api.dto.instructor.CreatedInstructorResponse;
import local.sop.sopinfo.registration.saga.application.api.dto.instructor.InstructorResponse;
import local.sop.sopinfo.registration.saga.application.api.dto.login.CreateLoginCmd;
import local.sop.sopinfo.registration.saga.application.api.dto.login.LoginResponse;
import local.sop.sopinfo.registration.saga.application.api.dto.organization.OrganisationResponse;
import local.sop.sopinfo.registration.saga.application.api.dto.person.CreatePersonCmd;
import local.sop.sopinfo.registration.saga.application.api.dto.person.PersonResponse;
import local.sop.sopinfo.registration.saga.application.infrastructure.response.ResponseLoginCreated;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.sopinfo.registration.saga.application.ports.out.apprentice.ApprenticePort;
import local.sop.sopinfo.registration.saga.application.ports.out.auditlog.AuditlogPort;
import local.sop.sopinfo.registration.saga.application.ports.out.consent.ConsentPort;
import local.sop.sopinfo.registration.saga.application.ports.out.educationline.EducationLinePort;
import local.sop.sopinfo.registration.saga.application.ports.out.instructor.InstructorPort;
import local.sop.sopinfo.registration.saga.application.ports.out.login.LoginPort;
import local.sop.sopinfo.registration.saga.application.ports.out.organization.OrganizationPort;
import local.sop.sopinfo.registration.saga.application.ports.out.person.PersonPort;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;

@Service
public class RegistrationSagaApplicationService implements RegistrationDirectory {

    private static final Logger log = LoggerFactory.getLogger(RegistrationSagaApplicationService.class);

    private final ApprenticePort apprentices;
    private final AuditlogPort auditlogs;
    private final ConsentPort consents;
    private final EducationLinePort educationLines;
    private final InstructorPort instructors;
    private final LoginPort logins;
    private final OrganizationPort organizations;
    private final PersonPort persons;

    public RegistrationSagaApplicationService(ApprenticePort apprentices, AuditlogPort auditlogs,
            EducationLinePort educationLines, ConsentPort consents, InstructorPort instructors, LoginPort logins,
            OrganizationPort organizations, PersonPort persons) {
        this.apprentices = apprentices;
        this.auditlogs = auditlogs;
        this.educationLines = educationLines;
        this.consents = consents;
        this.instructors = instructors;
        this.logins = logins;
        this.organizations = organizations;
        this.persons = persons;
    }

    /*
     * 1. Verify organization exists
     * 1.1 success
     * 1.2 failure -> throw exception
     * 2. Verify education line exists
     * 2.1 success
     * 2.2 failure -> throw exception
     * 3. Verify consent statement exists
     * 3.1 success
     * 3.2 failure -> throw exception
     * 4. Create a person
     * 4.1 success
     * 4.2 failure -> throw exception
     * 5. Get the person (sanity check)
     * 5.1 success
     * 5.2 failure -> compensate person -> throw exception
     * 6. Create a login
     * 6.1 success
     * 6.2 failure -> compensate person -> throw exception
     * 7. Get the login (sanity check)
     * 7.1 success
     * 7.2 failure -> compensate login and person -> throw exception
     * 8. Create apprentice
     * 8.1 success
     * 8.2 failure -> compensate login and person -> throw exception
     * 9. Get the apprentice (sanity check)
     * 9.1 success
     * 9.2 failure -> compensate apprentice, login and person -> throw exception
     * 10. Grant consent
     * 10.1 success
     * 10.2 failure -> compensate apprentice, login and person -> throw exception
     * 11. Get the consent (sanity check)
     * 11.1 success
     * 11.2 failure -> compensate consent, apprentice, login and person -> throw
     * exception
     * 12. Create audit log
     * 12.1 success
     * 12.2 failure -> compensate consent, apprentice, login and person -> throw
     * exception
     * 13. Get the audit log (sanity check)
     * 13.1 success
     * 13.2 failure -> compensate audit log, consent, apprentice, login and person
     * -> throw exception
     * 14. Return success
     */

    @Override
    public CreatedApprenticeResponse registerApprentice(CreateApprenticeRegistrationCmd cmd) {
        UUID personId = null;
        UUID loginId = null;
        UUID apprenticeId = null;
        UUID consentId = null;
        UUID auditlogId = null;

        // Step 1, 2 and 3: Verify organization, education line and consent statement
        // exists
        verifyOrganizationExists(cmd.organizationRef());
        verifyEducationLineExists(cmd.educationLineRef());
        verifyConsentStatementExists(cmd.consentStatementId());

        // Step 4: Create a person
        try {
            personId = persons.create(
                    new CreatePersonCmd(cmd.firstName(), cmd.lastName(), cmd.email(), cmd.organizationRef(),
                            cmd.phoneNumbers()));
        } catch (ConflictException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Registration: person creation failed", ex);
            throw new ConflictException("person.not.created", Map.of("", "person"));
        }

        // Step 5: Get the person (sanity check)
        PersonResponse person;
        try {
            person = persons.getById(personId);
            if (person == null) {
                log.warn("Registration: sanity checking person returned null for id: {}", personId);
                compensatePerson(personId);
                throw new ConflictException("person.not.found", Map.of("id", personId.toString()));
            }
        } catch (ConflictException ex) {
            throw ex;
        } catch (NotFoundException ex) {
            compensatePerson(personId);
            throw new ConflictException("person.not.found", Map.of("id", personId.toString()));
        } catch (Exception ex) {
            log.warn("Registration: failed to retrieve person with id: {}", personId, ex);
            compensatePerson(personId);
            throw new ConflictException("person.read.failed", Map.of("id", personId.toString()));
        }

        // Step 6: Create a login
        loginId = createLogin(new CreateLoginCmd(personId, cmd.username(), cmd.status()));

        // Step 7: Get the login (sanity check)
        LoginResponse login;
        try {
            login = logins.getById(loginId);
            if (login == null) {
                log.warn("Registration: sanity checking login returned null for id: {}", loginId);
                compensateLogin(loginId);
                compensatePerson(personId);
                throw new ConflictException("login.not.found", Map.of("id", loginId.toString()));
            }
        } catch (ConflictException ex) {
            throw ex;
        } catch (NotFoundException ex) {
            compensateLogin(loginId);
            compensatePerson(personId);
            throw new ConflictException("login.not.found", Map.of("id", loginId.toString()));
        } catch (Exception ex) {
            log.warn("Registration: failed to retrieve login with id: {}", loginId, ex);
            compensateLogin(loginId);
            compensatePerson(personId);
            throw new ConflictException("login.read.failed", Map.of("id", loginId.toString()));
        }

        // Step 8: Create apprentice
        try {
            apprenticeId = apprentices.create(new CreateApprenticeCmd(personId, cmd.educationLineRef()));
        } catch (ConflictException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Registration: apprentice creation failed", ex);
            compensateLogin(loginId);
            compensatePerson(personId);
            throw new ConflictException("apprentice.not.created", Map.of("", "apprentice"));
        }

        // Step 9: Get the apprentice (sanity check)
        ApprenticeResponse apprentice;
        try {
            apprentice = apprentices.getById(apprenticeId);
            if (apprentice == null) {
                log.warn("Registration: sanity checking apprentice returned null for id: {}", apprenticeId);
                compensateApprentice(apprenticeId);
                compensateLogin(loginId);
                compensatePerson(personId);
                throw new ConflictException("apprentice.not.found", Map.of("id", apprenticeId.toString()));
            }
        } catch (ConflictException ex) {
            throw ex;
        } catch (NotFoundException ex) {
            compensateApprentice(apprenticeId);
            compensateLogin(loginId);
            compensatePerson(personId);
            throw new ConflictException("apprentice.not.found", Map.of("id", apprenticeId.toString()));
        } catch (Exception ex) {
            log.warn("Registration: failed to retrieve apprentice with id: {}", apprenticeId, ex);
            compensateApprentice(apprenticeId);
            compensateLogin(loginId);
            compensatePerson(personId);
            throw new ConflictException("apprentice.read.failed", Map.of("id", apprenticeId.toString()));
        }

        // Step 10: Grant consent
        try {
            consentId = consents
                    .grant(new GrantConsentCmd(personId, cmd.consentStatementId(), cmd.purpose(), cmd.type(),
                            cmd.consentStatus()));
        } catch (ConflictException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Registration: consent grant failed", ex);
            compensateApprentice(apprenticeId);
            compensateLogin(loginId);
            compensatePerson(personId);
            throw new ConflictException("consent.not.granted", Map.of("", "consent"));
        }

        // Step 11: Get the consent (sanity check)
        ConsentResponse consent;
        try {
            consent = consents.getById(consentId);
            if (consent == null) {
                log.warn("Registration: sanity checking consent returned null for id: {}", consentId);
                compensateConsent(consentId);
                compensateApprentice(apprenticeId);
                compensateLogin(loginId);
                compensatePerson(personId);
                throw new ConflictException("consent.not.found", Map.of("id", consentId.toString()));
            }
        } catch (ConflictException ex) {
            throw ex;
        } catch (NotFoundException ex) {
            compensateConsent(consentId);
            compensateApprentice(apprenticeId);
            compensateLogin(loginId);
            compensatePerson(personId);
            throw new ConflictException("consent.not.found", Map.of("id", consentId.toString()));
        } catch (Exception ex) {
            log.warn("Registration: failed to retrieve consent with id: {}", consentId, ex);
            compensateConsent(consentId);
            compensateApprentice(apprenticeId);
            compensateLogin(loginId);
            compensatePerson(personId);
            throw new ConflictException("consent.read.failed", Map.of("id", consentId.toString()));
        }

        // Step 12: Create audit log
        try {
            auditlogId = auditlogs.create(new CreateAuditlogCmd(
                    personId,
                    local.sop.common.libs.sharedkernel.enums.ActorType.SERVICE,
                    local.sop.common.libs.sharedkernel.enums.Severity.INFO,
                    "registration-saga",
                    "RegistrationSagaApplicationService",
                    "registerApprentice",
                    String.format("Registered apprentice with id: %s", apprenticeId),
                    ("Apprentice created through registration-saga")));
        } catch (ConflictException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Registration: auditlog creation failed", ex);
            compensateConsent(consentId);
            compensateApprentice(apprenticeId);
            compensateLogin(loginId);
            compensatePerson(personId);
            throw new ConflictException("auditlog.not.created", Map.of("", "auditlog"));
        }

        // Step 13: Get the audit log (sanity check)
        AuditlogResponse auditlog;
        try {
            auditlog = auditlogs.getById(auditlogId);
            if (auditlog == null) {
                log.warn("Registration: sanity checking auditlog returned null with id: {}", auditlogId);
                compensateAuditlog(auditlogId);
                compensateConsent(consentId);
                compensateApprentice(apprenticeId);
                compensateLogin(loginId);
                compensatePerson(personId);
                throw new ConflictException("auditlog.not.found", Map.of("id", auditlogId.toString()));
            }
        } catch (ConflictException ex) {
            throw ex;
        } catch (NotFoundException ex) {
            compensateAuditlog(auditlogId);
            compensateConsent(consentId);
            compensateApprentice(apprenticeId);
            compensateLogin(loginId);
            compensatePerson(personId);
            throw new ConflictException("auditlog.not.found", Map.of("id", auditlogId.toString()));
        } catch (Exception ex) {
            log.warn(
                    "Registration: failed to retrieve auditlog with id: {}", auditlogId, ex);
            compensateAuditlog(auditlogId);
            compensateConsent(consentId);
            compensateApprentice(apprenticeId);
            compensateLogin(loginId);
            compensatePerson(personId);
            throw new ConflictException("auditlog.read.failed", Map.of("id", auditlogId.toString()));
        }

        // Step 14: Return success
        return new CreatedApprenticeResponse(apprenticeId);
    }

    /*
     * 1. Verify organization exists
     * 1.1 success
     * 1.2 failure -> throw exception
     * 2. Verify consent statement exists
     * 2.1 success
     * 2.2 failure -> throw exception
     * 3. Create a person
     * 3.1 success
     * 3.2 failure -> throw exception
     * 4. Get the person (sanity check)
     * 4.1 success
     * 4.2 failure -> compensate person -> throw exception
     * 5. Create a login
     * 5.1 success
     * 5.2 failure -> compensate person -> throw exception
     * 6. Get the login (sanity check)
     * 6.1 success
     * 6.2 failure -> compensate login and person -> throw exception
     * 7. Create instructor
     * 7.1 success
     * 7.2 failure -> compensate login and person -> throw exception
     * 8. Get the instructor (sanity check)
     * 8.1 success
     * 8.2 failure -> compensate instructor, login and person -> throw exception
     * 9. Grant consent
     * 9.1 success
     * 9.2 failure -> compensate instructor, login and person -> throw exception
     * 10. Get the consent (sanity check)
     * 10.1 success
     * 10.2 failure -> compensate consent, instructor, login and person -> throw
     * exception
     * 11. Create audit log
     * 11.1 success
     * 11.2 failure -> compensate consent, instructor, login and person -> throw
     * exception
     * 12. Get the audit log (sanity check)
     * 12.1 success
     * 12.2 failure -> compensate audit log, consent, instructor, login and person
     * -> throw exception
     * 13. Return success
     */
    @Override
    public CreatedInstructorResponse registerInstructor(CreateInstructorRegistrationCmd cmd) {
        UUID personId = null;
        UUID loginId = null;
        UUID instructorId = null;
        UUID consentId = null;
        UUID auditlogId = null;

        // Step 1 and 2: Verify organization and consent statement exists
        verifyOrganizationExists(cmd.organizationRef());
        verifyConsentStatementExists(cmd.consentStatementId());

        // Step 3: Create a person
        try {
            personId = persons.create(
                    new CreatePersonCmd(cmd.firstName(), cmd.lastName(), cmd.email(), cmd.organizationRef(),
                            cmd.phoneNumbers()));
        } catch (ConflictException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Registration: person creation failed", ex);
            throw new ConflictException("person.not.created", Map.of("", "person"));
        }

        // Step 4: Get the person (sanity check)
        PersonResponse person;
        try {
            person = persons.getById(personId);
            if (person == null) {
                log.warn("Registration: sanity checking person returned null for id: {}", personId);
                compensatePerson(personId);
                throw new ConflictException("person.not.found", Map.of("id", personId.toString()));
            }
        } catch (ConflictException ex) {
            throw ex;
        } catch (NotFoundException ex) {
            compensatePerson(personId);
            throw new ConflictException("person.not.found", Map.of("id", personId.toString()));
        } catch (Exception ex) {
            log.warn("Registration: failed to retrieve person with id: {}", personId, ex);
            compensatePerson(personId);
            throw new ConflictException("person.read.failed", Map.of("id", personId.toString()));
        }

        // Step 5: Create a login
        loginId = createLogin(new CreateLoginCmd(personId, cmd.username(), cmd.status()));

        // Step 6: Get the login (sanity check)
        LoginResponse login;
        try {
            login = logins.getById(loginId);
            if (login == null) {
                log.warn("Registration: sanity checking login returned null for id: {}", loginId);
                compensateLogin(loginId);
                compensatePerson(personId);
                throw new ConflictException("login.not.found", Map.of("id", loginId.toString()));
            }
        } catch (ConflictException ex) {
            throw ex;
        } catch (NotFoundException ex) {
            compensateLogin(loginId);
            compensatePerson(personId);
            throw new ConflictException("login.not.found", Map.of("id", loginId.toString()));
        } catch (Exception ex) {
            log.warn("Registration: failed to retrieve login with id: {}", loginId, ex);
            compensateLogin(loginId);
            compensatePerson(personId);
            throw new ConflictException("login.read.failed", Map.of("id", loginId.toString()));
        }

        // Step 7: Create instructor
        try {
            instructorId = instructors.create(new CreateInstructorCmd(personId));
        } catch (ConflictException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Registration: instructor creation failed", ex);
            compensateLogin(loginId);
            compensatePerson(personId);
            throw new ConflictException("instructor.not.created", Map.of("", "instructor"));
        }

        // Step 8: Get the instructor (sanity check)

        InstructorResponse instructor;
        try {
            instructor = instructors.getById(instructorId);
            if (instructor == null) {
                log.warn("Registration: sanity checking instructor returned null for id: {}", instructorId);
                compensateInstructor(instructorId);
                compensateLogin(loginId);
                compensatePerson(personId);
                throw new ConflictException("instructor.not.found", Map.of("id", instructorId.toString()));
            }
        } catch (ConflictException ex) {
            throw ex;
        } catch (NotFoundException ex) {
            compensateInstructor(instructorId);
            compensateLogin(loginId);
            compensatePerson(personId);
            throw new ConflictException("instructor.not.found", Map.of("id", instructorId.toString()));
        } catch (Exception ex) {
            log.warn("Registration: failed to retrieve instructor with id: {}", instructorId, ex);
            compensateInstructor(instructorId);
            compensateLogin(loginId);
            compensatePerson(personId);
            throw new ConflictException("instructor.read.failed", Map.of("id", instructorId.toString()));
        }

        // Step 9: Grant consent
        try {
            consentId = consents
                    .grant(new GrantConsentCmd(personId, cmd.consentStatementId(), cmd.purpose(), cmd.type(),
                            cmd.consentStatus()));
        } catch (ConflictException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Registration: consent grant failed", ex);
            compensateInstructor(instructorId);
            compensateLogin(loginId);
            compensatePerson(personId);
            throw new ConflictException("consent.not.granted", Map.of("", "consent"));
        }

        // Step 10: Get the consent (sanity check)
        ConsentResponse consent;
        try {
            consent = consents.getById(consentId);
            if (consent == null) {
                log.warn("Registration: sanity checking consent returned null for id: {}", consentId);
                compensateConsent(consentId);
                compensateInstructor(instructorId);
                compensateLogin(loginId);
                compensatePerson(personId);
                throw new ConflictException("consent.not.found", Map.of("id", consentId.toString()));
            }
        } catch (ConflictException ex) {
            throw ex;
        } catch (NotFoundException ex) {
            compensateConsent(consentId);
            compensateInstructor(instructorId);
            compensateLogin(loginId);
            compensatePerson(personId);
            throw new ConflictException("consent.not.found", Map.of("id", consentId.toString()));
        } catch (Exception ex) {
            log.warn("Registration: failed to retrieve consent with id: {}", consentId, ex);
            compensateConsent(consentId);
            compensateInstructor(instructorId);
            compensateLogin(loginId);
            compensatePerson(personId);
            throw new ConflictException("consent.read.failed", Map.of("id", consentId.toString()));
        }

        // Step 11: Create audit log
        try {
            auditlogId = auditlogs.create(new CreateAuditlogCmd(
                    personId,
                    local.sop.common.libs.sharedkernel.enums.ActorType.SERVICE,
                    local.sop.common.libs.sharedkernel.enums.Severity.INFO,
                    "registration-saga",
                    "RegistrationSagaApplicationService",
                    "registerInstructor",
                    String.format("Registered instructor with id: %s", instructorId),
                    ("Instructor created through registration-saga")));
        } catch (ConflictException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Registration: auditlog creation failed", ex);
            compensateConsent(consentId);
            compensateInstructor(instructorId);
            compensateLogin(loginId);
            compensatePerson(personId);
            throw new ConflictException("auditlog.not.created", Map.of("", "auditlog"));
        }

        // Step 12: Get the audit log (sanity check)
        AuditlogResponse auditlog;
        try {
            auditlog = auditlogs.getById(auditlogId);
            if (auditlog == null) {
                log.warn("Registration: sanity checking auditlog returned null with id: {}", auditlogId);
                compensateAuditlog(auditlogId);
                compensateConsent(consentId);
                compensateInstructor(instructorId);
                compensateLogin(loginId);
                compensatePerson(personId);
                throw new ConflictException("auditlog.not.found", Map.of("id", auditlogId.toString()));
            }
        } catch (ConflictException ex) {
            throw ex;
        } catch (NotFoundException ex) {
            compensateAuditlog(auditlogId);
            compensateConsent(consentId);
            compensateInstructor(instructorId);
            compensateLogin(loginId);
            compensatePerson(personId);
            throw new ConflictException("auditlog.not.found", Map.of("id", auditlogId.toString()));
        } catch (Exception ex) {
            log.warn(
                    "Registration: failed to retrieve auditlog with id: {}", auditlogId, ex);
            compensateAuditlog(auditlogId);
            compensateConsent(consentId);
            compensateInstructor(instructorId);
            compensateLogin(loginId);
            compensatePerson(personId);
            throw new ConflictException("auditlog.read.failed", Map.of("id", auditlogId.toString()));
        }

        // Step 13: Return success
        return new CreatedInstructorResponse(instructorId);
    }

    // Helper methods

    private ResponseCompensated compensatePerson(UUID personId) {
        ResponseCompensated result = persons.compensate(personId, getClass(), SagaOutcome.COMPENSATED);
        log.warn("Registration: compensated person id: {}. compensated. Success: {}", personId, result.success());
        return result;
    }

    private ResponseCompensated compensateLogin(UUID loginId) {
        ResponseCompensated result = logins.compensate(loginId, getClass(), SagaOutcome.COMPENSATED);
        log.warn("Registration: compensated login id: {}. compensated. Success: {}", loginId, result.success());
        return result;
    }

    private ResponseCompensated compensateApprentice(UUID apprenticeId) {
        ResponseCompensated result = apprentices.compensate(apprenticeId, getClass(), SagaOutcome.COMPENSATED);
        log.warn("Registration: compensated apprentice id: {}. compensated. Success: {}", apprenticeId,
                result.success());
        return result;
    }

    private ResponseCompensated compensateInstructor(UUID instructorId) {
        ResponseCompensated result = instructors.compensate(instructorId, getClass(), SagaOutcome.COMPENSATED);
        log.warn("Registration: compensated instructor id: {}. compensated. Success: {}", instructorId,
                result.success());
        return result;
    }

    private ResponseCompensated compensateConsent(UUID consentId) {
        ResponseCompensated result = consents.compensate(consentId, getClass(), SagaOutcome.COMPENSATED);
        log.warn("Registration: compensated consent id: {}. compensated. Success: {}", consentId, result.success());
        return result;
    }

    private ResponseCompensated compensateAuditlog(UUID auditlogId) {
        ResponseCompensated result = auditlogs.compensate(auditlogId, getClass(), SagaOutcome.COMPENSATED);
        log.warn("Registration: compensated auditlog id: {}. compensated. Success: {}", auditlogId, result.success());
        return result;
    }


    private OrganisationResponse verifyOrganizationExists(UUID organizationId) {
        OrganisationResponse organization;
        try {
            organization = organizations.getById(organizationId);

            if (organization == null) {
                log.warn("Registration: organization returned null for id: {}", organizationId);
                throw new ConflictException("organization.not.found", Map.of("id", organizationId.toString()));
            }
        } catch (ConflictException ex) {
            throw ex;
        } catch (NotFoundException ex) {
            throw new ConflictException("organization.not.found", Map.of("id", organizationId.toString()));
        } catch (Exception ex) {
            log.warn("Registration: unexpected error while retrieving organization with id: {}", organizationId, ex);
            throw new ConflictException("organization.read.failed", Map.of("id", organizationId.toString()));
        }
        return organization;
    }

    private EducationLineResponse verifyEducationLineExists(UUID educationLineId) {
        EducationLineResponse educationLine;
        try {
            educationLine = educationLines.getById(educationLineId);

            if (educationLine == null) {
                log.warn("Registration: education line returned null for id: {}", educationLineId);
                throw new ConflictException("educationline.not.found", Map.of("id", educationLineId.toString()));
            }
        } catch (ConflictException ex) {
            throw ex;
        } catch (NotFoundException ex) {
            throw new ConflictException("educationline.not.found", Map.of("id", educationLineId.toString()));
        } catch (Exception ex) {
            log.warn("Registration: unexpected error while retrieving education line with id: {}", educationLineId, ex);
            throw new ConflictException("educationline.read.failed", Map.of("id", educationLineId.toString()));
        }
        return educationLine;
    }

    private ConsentStatementResponse verifyConsentStatementExists(UUID consentStatementId) {
        ConsentStatementResponse consentStatement;
        try {
            consentStatement = consents.getConsentStatementById(consentStatementId);

            if (consentStatement == null) {
                log.warn("Registration: consent statement returned null for id: {}", consentStatementId);
                throw new ConflictException("consentstatement.not.found", Map.of("id", consentStatementId.toString()));
            }
        } catch (ConflictException ex) {
            throw ex;
        } catch (NotFoundException ex) {
            throw new ConflictException("consentstatement.not.found", Map.of("id", consentStatementId.toString()));
        } catch (Exception ex) {
            log.warn("Registration: failed to retrieve consent statement with id: {}", consentStatementId, ex);
            throw new ConflictException("consentstatement.read.failed", Map.of("id", consentStatementId.toString()));
        }
        return consentStatement;
    }

    private UUID createLogin(CreateLoginCmd cmd) {
        ResponseLoginCreated loginCreatedResponse;
        UUID loginId;
        try {
            loginCreatedResponse = logins.create(new CreateLoginCmd(cmd.personRef(), cmd.username(), cmd.status()));
            loginId = loginCreatedResponse.id();
        } catch (ConflictException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Registration: login creation failed", ex);
            compensatePerson(cmd.personRef());
            throw new ConflictException("login.not.created", Map.of("object", "login"));
        }
        return loginId;
    }
}
