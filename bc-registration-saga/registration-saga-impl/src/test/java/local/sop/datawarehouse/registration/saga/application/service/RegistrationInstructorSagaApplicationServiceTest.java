package local.sop.datawarehouse.registration.saga.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentStatus;
import local.sop.common.libs.sharedkernel.enums.ConsentType;
import local.sop.common.libs.sharedkernel.enums.PhoneUserType;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.registration.saga.application.api.dto.CreateInstructorRegistrationCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.CreateInstructorRegistrationCmd.ConsentStatement;
import local.sop.datawarehouse.registration.saga.application.api.dto.auditlog.AuditlogResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.consent.ConsentResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.consent.GrantConsentCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.instructor.CreatedInstructorResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.instructor.InstructorResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.login.LoginResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.organization.OrganisationResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.person.CreatePhoneNumberCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.person.PersonResponse;
import local.sop.datawarehouse.registration.saga.application.infrastructure.response.ResponseLoginCreated;
import local.sop.datawarehouse.registration.saga.application.ports.out.apprentice.ApprenticePort;
import local.sop.datawarehouse.registration.saga.application.ports.out.auditlog.AuditlogPort;
import local.sop.datawarehouse.registration.saga.application.ports.out.consent.ConsentPort;
import local.sop.datawarehouse.registration.saga.application.ports.out.consentsaga.ConsentSagaPort;
import local.sop.datawarehouse.registration.saga.application.ports.out.educationline.EducationLinePort;
import local.sop.datawarehouse.registration.saga.application.ports.out.instructor.InstructorPort;
import local.sop.datawarehouse.registration.saga.application.ports.out.login.LoginPort;
import local.sop.datawarehouse.registration.saga.application.ports.out.organization.OrganizationPort;
import local.sop.datawarehouse.registration.saga.application.ports.out.person.PersonPort;

/**
 * Full rewrite against the CURRENT service — same reasoning as
 * RegistrationApprenticesSagaApplicationServiceTest. registerInstructor()
 * has no education-line step and creates an Instructor rather than an
 * Apprentice; otherwise the shape (person -> login -> [role] -> consent
 * -> auditlog, each with a sanity check and a compensation cascade) is
 * the same pattern.
 */
@ExtendWith(MockitoExtension.class)
class RegistrationInstructorSagaApplicationServiceTest {

    @Mock private ApprenticePort apprentices;
    @Mock private AuditlogPort auditlogs;
    @Mock private ConsentPort consents;
    @Mock private ConsentSagaPort consentSagas;
    @Mock private EducationLinePort educationLines;
    @Mock private InstructorPort instructors;
    @Mock private LoginPort logins;
    @Mock private OrganizationPort organizations;
    @Mock private PersonPort persons;

    private RegistrationSagaApplicationService service;

    private static final UUID ORG_REF = UUID.randomUUID();
    private static final UUID PERSON_ID = UUID.randomUUID();
    private static final UUID LOGIN_ID = UUID.randomUUID();
    private static final UUID INSTRUCTOR_ID = UUID.randomUUID();
    private static final UUID AUDITLOG_ID = UUID.randomUUID();
    private static final UUID CONSENT_STATEMENT_REF = UUID.randomUUID();
    private static final UUID CONSENT_ID = UUID.randomUUID();

    private CreateInstructorRegistrationCmd cmd;

    @BeforeEach
    void setUp() {
        service = new RegistrationSagaApplicationService(apprentices, auditlogs, educationLines, consentSagas,
                consents, instructors, logins, organizations, persons);

        cmd = new CreateInstructorRegistrationCmd(
                "Kris", "K", "kris@example.com", ORG_REF,
                List.of(new CreatePhoneNumberCmd(PhoneUserType.SELF, "+4587654321")),
                "krisk", "ACTIVE",
                List.of(new ConsentStatement(CONSENT_STATEMENT_REF, ConsentStatus.ACTIVE)));
    }

    // ---- shared stub helpers -------------------------------------------------

    private void stubOrganizationOk() {
        when(organizations.getById(ORG_REF)).thenReturn(new OrganisationResponse(ORG_REF, "ZBC", "87654321"));
    }

    private void stubPersonOk() {
        when(persons.create(any())).thenReturn(PERSON_ID);
        when(persons.getById(PERSON_ID)).thenReturn(new PersonResponse(PERSON_ID, "Kris", "K", "kris@example.com", ORG_REF, List.of()));
    }

    private void stubLoginOk() {
        when(logins.create(any())).thenReturn(new ResponseLoginCreated(LOGIN_ID, "pw", null));
        when(logins.getById(LOGIN_ID)).thenReturn(new LoginResponse(LOGIN_ID, PERSON_ID, "krisk", "ACTIVE"));
    }

    private void stubInstructorOk() {
        when(instructors.create(any())).thenReturn(INSTRUCTOR_ID);
        when(instructors.getById(INSTRUCTOR_ID)).thenReturn(new InstructorResponse(INSTRUCTOR_ID, PERSON_ID));
    }

    private void stubConsentGrantOk() {
        when(consentSagas.grant(any(GrantConsentCmd.class))).thenReturn(
                new ConsentResponse(CONSENT_ID, PERSON_ID, "ACTIVE", CONSENT_STATEMENT_REF, "T&C", ConsentPurpose.REQUIRED_SERVICE, ConsentType.REQUIRED, true));
        when(consents.getById(CONSENT_ID)).thenReturn(
                new ConsentResponse(CONSENT_ID, PERSON_ID, "ACTIVE", CONSENT_STATEMENT_REF, "T&C", ConsentPurpose.REQUIRED_SERVICE, ConsentType.REQUIRED, true));
    }

    private void stubAuditlogOk() {
        when(auditlogs.create(any())).thenReturn(AUDITLOG_ID);
        when(auditlogs.getById(AUDITLOG_ID)).thenReturn(
                new AuditlogResponse(AUDITLOG_ID, PERSON_ID, null, null, "registration-saga", "svc", "comp", "data", "desc", null));
    }

    private ResponseCompensated ok() {
        return new ResponseCompensated(local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome.COMPENSATED, true);
    }

    private void stubAllCompensationsOk() {
        // lenient() — see the same helper in
        // RegistrationApprenticesSagaApplicationServiceTest for why.
        org.mockito.Mockito.lenient().when(persons.compensate(any(), any(), any())).thenReturn(ok());
        org.mockito.Mockito.lenient().when(logins.compensate(any(), any(), any())).thenReturn(ok());
        org.mockito.Mockito.lenient().when(instructors.compensate(any(), any(), any())).thenReturn(ok());
        org.mockito.Mockito.lenient().when(consentSagas.compensateConsent(any(), any(), any())).thenReturn(ok());
        org.mockito.Mockito.lenient().when(auditlogs.compensate(any(), any(), any())).thenReturn(ok());
    }

    // ---- happy path ------------------------------------------------------

    @Nested
    class HappyPath {

        @Test
        void allStepsSucceed_returnsCreatedInstructorResponse() {
            stubOrganizationOk();
            stubPersonOk();
            stubLoginOk();
            stubInstructorOk();
            stubConsentGrantOk();
            stubAuditlogOk();

            CreatedInstructorResponse result = service.registerInstructor(cmd);

            assertEquals(INSTRUCTOR_ID, result.id());
            verify(persons).create(any());
            verify(logins).create(any());
            verify(instructors).create(any());
            verify(consentSagas).grant(any(GrantConsentCmd.class));
            verify(auditlogs).create(any());
            verify(persons, never()).compensate(any(), any(), any());
            verify(logins, never()).compensate(any(), any(), any());
            verify(instructors, never()).compensate(any(), any(), any());
            verify(consentSagas, never()).compensateConsent(any(), any(), any());
        }

        @Test
        void multipleConsentStatements_grantsAndSanityChecksEachOne() {
            UUID secondStatementRef = UUID.randomUUID();
            UUID secondConsentId = UUID.randomUUID();
            cmd = new CreateInstructorRegistrationCmd(
                    cmd.firstName(), cmd.lastName(), cmd.email(), cmd.organizationRef(), cmd.phoneNumbers(),
                    cmd.username(), cmd.status(),
                    List.of(new ConsentStatement(CONSENT_STATEMENT_REF, ConsentStatus.ACTIVE),
                            new ConsentStatement(secondStatementRef, ConsentStatus.ACTIVE)));

            stubOrganizationOk();
            stubPersonOk();
            stubLoginOk();
            stubInstructorOk();
            stubAuditlogOk();

            when(consentSagas.grant(any(GrantConsentCmd.class)))
                    .thenReturn(new ConsentResponse(CONSENT_ID, PERSON_ID, "ACTIVE", CONSENT_STATEMENT_REF, "T&C", ConsentPurpose.REQUIRED_SERVICE, ConsentType.REQUIRED, true))
                    .thenReturn(new ConsentResponse(secondConsentId, PERSON_ID, "ACTIVE", secondStatementRef, "Notifications", ConsentPurpose.COMMUNICATION, ConsentType.OPTIONAL, true));
            when(consents.getById(CONSENT_ID)).thenReturn(
                    new ConsentResponse(CONSENT_ID, PERSON_ID, "ACTIVE", CONSENT_STATEMENT_REF, "T&C", ConsentPurpose.REQUIRED_SERVICE, ConsentType.REQUIRED, true));
            when(consents.getById(secondConsentId)).thenReturn(
                    new ConsentResponse(secondConsentId, PERSON_ID, "ACTIVE", secondStatementRef, "Notifications", ConsentPurpose.COMMUNICATION, ConsentType.OPTIONAL, true));

            CreatedInstructorResponse result = service.registerInstructor(cmd);

            assertEquals(INSTRUCTOR_ID, result.id());
            verify(consentSagas, times(2)).grant(any(GrantConsentCmd.class));
            verify(consents).getById(CONSENT_ID);
            verify(consents).getById(secondConsentId);
        }
    }

    // ---- pre-flight verification -----------------------------------------

    @Nested
    class PreFlightVerification {

        @Test
        void organizationNotFound_throwsConflictExceptionWithNoSideEffects() {
            when(organizations.getById(ORG_REF)).thenReturn(null);

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerInstructor(cmd));
            assertEquals("organization.not.found", ex.getMessage());
            verify(persons, never()).create(any());
        }
    }

    // ---- person step -------------------------------------------------------

    @Nested
    class PersonStep {

        @Test
        void creationFails_throwsConflictException_noCompensation() {
            stubOrganizationOk();
            when(persons.create(any())).thenThrow(new RuntimeException("boom"));

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerInstructor(cmd));
            assertEquals("person.not.created", ex.getMessage());
            verify(persons, never()).compensate(any(), any(), any());
        }

        @Test
        void sanityCheckReturnsNull_compensatesPerson() {
            stubOrganizationOk();
            when(persons.create(any())).thenReturn(PERSON_ID);
            when(persons.getById(PERSON_ID)).thenReturn(null);
            when(persons.compensate(eq(PERSON_ID), any(), any())).thenReturn(ok());

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerInstructor(cmd));
            assertEquals("person.not.found", ex.getMessage());
            verify(persons).compensate(eq(PERSON_ID), any(), any());
        }

        @Test
        void sanityCheckThrowsNotFound_compensatesPerson() {
            stubOrganizationOk();
            when(persons.create(any())).thenReturn(PERSON_ID);
            when(persons.getById(PERSON_ID)).thenThrow(new NotFoundException("person.notfound", java.util.Map.of()));
            when(persons.compensate(eq(PERSON_ID), any(), any())).thenReturn(ok());

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerInstructor(cmd));
            assertEquals("person.not.found", ex.getMessage());
            verify(persons).compensate(eq(PERSON_ID), any(), any());
        }
    }

    // ---- login step ---------------------------------------------------------

    @Nested
    class LoginStep {

        @Test
        void creationFails_compensatesPersonOnly() {
            stubOrganizationOk();
            stubPersonOk();
            when(logins.create(any())).thenThrow(new RuntimeException("boom"));
            when(persons.compensate(any(), any(), any())).thenReturn(ok());

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerInstructor(cmd));
            assertEquals("login.not.created", ex.getMessage());
            verify(persons).compensate(eq(PERSON_ID), any(), any());
            verify(logins, never()).compensate(any(), any(), any());
        }

        @Test
        void sanityCheckReturnsNull_compensatesLoginAndPerson() {
            stubOrganizationOk();
            stubPersonOk();
            when(logins.create(any())).thenReturn(new ResponseLoginCreated(LOGIN_ID, "pw", null));
            when(logins.getById(LOGIN_ID)).thenReturn(null);
            stubAllCompensationsOk();

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerInstructor(cmd));
            assertEquals("login.not.found", ex.getMessage());
            verify(logins).compensate(eq(LOGIN_ID), any(), any());
            verify(persons).compensate(eq(PERSON_ID), any(), any());
        }
    }

    // ---- instructor step ------------------------------------------------------

    @Nested
    class InstructorStep {

        @Test
        void creationFails_compensatesLoginAndPerson() {
            stubOrganizationOk();
            stubPersonOk();
            stubLoginOk();
            when(instructors.create(any())).thenThrow(new RuntimeException("boom"));
            stubAllCompensationsOk();

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerInstructor(cmd));
            assertEquals("instructor.not.created", ex.getMessage());
            verify(logins).compensate(eq(LOGIN_ID), any(), any());
            verify(persons).compensate(eq(PERSON_ID), any(), any());
            verify(instructors, never()).compensate(any(), any(), any());
        }

        @Test
        void sanityCheckReturnsNull_compensatesInstructorLoginAndPerson() {
            stubOrganizationOk();
            stubPersonOk();
            stubLoginOk();
            when(instructors.create(any())).thenReturn(INSTRUCTOR_ID);
            when(instructors.getById(INSTRUCTOR_ID)).thenReturn(null);
            stubAllCompensationsOk();

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerInstructor(cmd));
            assertEquals("instructor.not.found", ex.getMessage());
            verify(instructors).compensate(eq(INSTRUCTOR_ID), any(), any());
            verify(logins).compensate(eq(LOGIN_ID), any(), any());
            verify(persons).compensate(eq(PERSON_ID), any(), any());
        }
    }

    // ---- consent step (grant via consentSagas, read via consents) --------

    @Nested
    class ConsentStep {

        @Test
        void grantFails_compensatesInstructorLoginAndPerson() {
            stubOrganizationOk();
            stubPersonOk();
            stubLoginOk();
            stubInstructorOk();
            when(consentSagas.grant(any(GrantConsentCmd.class))).thenThrow(new RuntimeException("boom"));
            stubAllCompensationsOk();

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerInstructor(cmd));
            assertEquals("consent.not.granted", ex.getMessage());
            verify(instructors).compensate(eq(INSTRUCTOR_ID), any(), any());
            verify(logins).compensate(eq(LOGIN_ID), any(), any());
            verify(persons).compensate(eq(PERSON_ID), any(), any());
        }

        @Test
        void sanityCheckReturnsNull_compensatesConsentInstructorLoginAndPerson() {
            stubOrganizationOk();
            stubPersonOk();
            stubLoginOk();
            stubInstructorOk();
            when(consentSagas.grant(any(GrantConsentCmd.class))).thenReturn(
                    new ConsentResponse(CONSENT_ID, PERSON_ID, "ACTIVE", CONSENT_STATEMENT_REF, "T&C", ConsentPurpose.REQUIRED_SERVICE, ConsentType.REQUIRED, true));
            when(consents.getById(CONSENT_ID)).thenReturn(null);
            stubAllCompensationsOk();

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerInstructor(cmd));
            assertEquals("consent.not.found", ex.getMessage());
            verify(consentSagas).compensateConsent(eq(CONSENT_ID), any(), any());
            verify(instructors).compensate(eq(INSTRUCTOR_ID), any(), any());
        }

        @Test
        void sanityCheckThrowsNotFound_compensatesConsentInstructorLoginAndPerson() {
            stubOrganizationOk();
            stubPersonOk();
            stubLoginOk();
            stubInstructorOk();
            when(consentSagas.grant(any(GrantConsentCmd.class))).thenReturn(
                    new ConsentResponse(CONSENT_ID, PERSON_ID, "ACTIVE", CONSENT_STATEMENT_REF, "T&C", ConsentPurpose.REQUIRED_SERVICE, ConsentType.REQUIRED, true));
            when(consents.getById(CONSENT_ID)).thenThrow(new NotFoundException("consent.notfound", java.util.Map.of()));
            stubAllCompensationsOk();

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerInstructor(cmd));
            assertEquals("consent.not.found", ex.getMessage());
            verify(consentSagas).compensateConsent(eq(CONSENT_ID), any(), any());
        }
    }

    // ---- audit log step -------------------------------------------------------

    @Nested
    class AuditlogStep {

        @Test
        void creationFails_compensatesConsentInstructorLoginAndPerson() {
            stubOrganizationOk();
            stubPersonOk();
            stubLoginOk();
            stubInstructorOk();
            stubConsentGrantOk();
            when(auditlogs.create(any())).thenThrow(new RuntimeException("boom"));
            stubAllCompensationsOk();

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerInstructor(cmd));
            assertEquals("auditlog.not.created", ex.getMessage());
            verify(consentSagas).compensateConsent(eq(CONSENT_ID), any(), any());
            verify(instructors).compensate(eq(INSTRUCTOR_ID), any(), any());
            verify(auditlogs, never()).compensate(any(), any(), any());
        }

        @Test
        void sanityCheckReturnsNull_compensatesEverything() {
            stubOrganizationOk();
            stubPersonOk();
            stubLoginOk();
            stubInstructorOk();
            stubConsentGrantOk();
            when(auditlogs.create(any())).thenReturn(AUDITLOG_ID);
            when(auditlogs.getById(AUDITLOG_ID)).thenReturn(null);
            stubAllCompensationsOk();

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerInstructor(cmd));
            assertEquals("auditlog.not.found", ex.getMessage());
            verify(auditlogs).compensate(eq(AUDITLOG_ID), any(), any());
            verify(consentSagas).compensateConsent(eq(CONSENT_ID), any(), any());
            verify(instructors).compensate(eq(INSTRUCTOR_ID), any(), any());
            verify(logins).compensate(eq(LOGIN_ID), any(), any());
            verify(persons).compensate(eq(PERSON_ID), any(), any());
        }
    }
}