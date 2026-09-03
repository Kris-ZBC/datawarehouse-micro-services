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

import local.sop.datawarehouse.sharedlib.enums.ConsentPurpose;
import local.sop.datawarehouse.sharedlib.enums.ConsentStatus;
import local.sop.datawarehouse.sharedlib.enums.ConsentType;
import local.sop.datawarehouse.sharedlib.enums.PhoneUserType;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.registration.saga.application.api.dto.CreateApprenticeRegistrationCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.CreateApprenticeRegistrationCmd.ConsentStatement;
import local.sop.datawarehouse.registration.saga.application.api.dto.apprentice.ApprenticeResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.apprentice.CreatedApprenticeResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.auditlog.AuditlogResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.consent.ConsentResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.consent.GrantConsentCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.educationline.EducationLineResponse;
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
 * Full rewrite against the CURRENT service, not a patch of the previous
 * version — the prior file predated both the ConsentPort/ConsentSagaPort
 * split and the move from a single consentStatementId to a
 * consentStatements list, and referenced methods/fields that no longer
 * exist on either. See registerApprentice()'s step comments in
 * RegistrationSagaApplicationService for the numbering used below.
 */
@ExtendWith(MockitoExtension.class)
class RegistrationApprenticesSagaApplicationServiceTest {

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
    private static final UUID EDUCATION_LINE_REF = UUID.randomUUID();
    private static final UUID PERSON_ID = UUID.randomUUID();
    private static final UUID LOGIN_ID = UUID.randomUUID();
    private static final UUID APPRENTICE_ID = UUID.randomUUID();
    private static final UUID AUDITLOG_ID = UUID.randomUUID();
    private static final UUID CONSENT_STATEMENT_REF = UUID.randomUUID();
    private static final UUID CONSENT_ID = UUID.randomUUID();

    private CreateApprenticeRegistrationCmd cmd;

    @BeforeEach
    void setUp() {
        service = new RegistrationSagaApplicationService(apprentices, auditlogs, educationLines, consentSagas,
                consents, instructors, logins, organizations, persons);

        cmd = new CreateApprenticeRegistrationCmd(
                "Daniel", "S", "dani@example.com", ORG_REF,
                List.of(new CreatePhoneNumberCmd(PhoneUserType.SELF, "+4512345678")),
                EDUCATION_LINE_REF, "danis", "ACTIVE",
                List.of(new ConsentStatement(CONSENT_STATEMENT_REF, ConsentStatus.ACTIVE)));
    }

    // ---- shared stub helpers -------------------------------------------------

    private void stubOrganizationAndEducationLineOk() {
        when(organizations.getById(ORG_REF)).thenReturn(new OrganisationResponse(ORG_REF, "Zealand", "12345678"));
        when(educationLines.getById(EDUCATION_LINE_REF))
                .thenReturn(new EducationLineResponse(EDUCATION_LINE_REF, "Data/IT", 4, 0, 0, UUID.randomUUID(), null, true));
    }

    private void stubPersonOk() {
        when(persons.create(any())).thenReturn(PERSON_ID);
        when(persons.getById(PERSON_ID)).thenReturn(new PersonResponse(PERSON_ID, "Daniel", "S", "dani@example.com", ORG_REF, List.of()));
    }

    private void stubLoginOk() {
        when(logins.create(any())).thenReturn(new ResponseLoginCreated(LOGIN_ID, "pw", null));
        when(logins.getById(LOGIN_ID)).thenReturn(new LoginResponse(LOGIN_ID, PERSON_ID, "danis", "ACTIVE"));
    }

    private void stubApprenticeOk() {
        when(apprentices.create(any())).thenReturn(APPRENTICE_ID);
        when(apprentices.getById(APPRENTICE_ID)).thenReturn(new ApprenticeResponse(APPRENTICE_ID, PERSON_ID, EDUCATION_LINE_REF));
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
        // lenient() because most individual tests only trigger a subset
        // of the compensation cascade (earlier failures compensate less
        // than later ones) — this helper stubs all five up front for
        // convenience, so Mockito's strict-stubbing check would flag
        // the ones a given test doesn't happen to invoke as unnecessary.
        org.mockito.Mockito.lenient().when(persons.compensate(any(), any(), any())).thenReturn(ok());
        org.mockito.Mockito.lenient().when(logins.compensate(any(), any(), any())).thenReturn(ok());
        org.mockito.Mockito.lenient().when(apprentices.compensate(any(), any(), any())).thenReturn(ok());
        org.mockito.Mockito.lenient().when(consentSagas.compensateConsent(any(), any(), any())).thenReturn(ok());
        org.mockito.Mockito.lenient().when(auditlogs.compensate(any(), any(), any())).thenReturn(ok());
    }

    // ---- happy path ------------------------------------------------------

    @Nested
    class HappyPath {

        @Test
        void allStepsSucceed_returnsCreatedApprenticeResponse() {
            stubOrganizationAndEducationLineOk();
            stubPersonOk();
            stubLoginOk();
            stubApprenticeOk();
            stubConsentGrantOk();
            stubAuditlogOk();

            CreatedApprenticeResponse result = service.registerApprentice(cmd);

            assertEquals(APPRENTICE_ID, result.apprenticeId());
            verify(persons).create(any());
            verify(logins).create(any());
            verify(apprentices).create(any());
            verify(consentSagas).grant(any(GrantConsentCmd.class));
            verify(auditlogs).create(any());
            // Nothing should have been compensated on the happy path.
            verify(persons, never()).compensate(any(), any(), any());
            verify(logins, never()).compensate(any(), any(), any());
            verify(apprentices, never()).compensate(any(), any(), any());
            verify(consentSagas, never()).compensateConsent(any(), any(), any());
        }

        @Test
        void multipleConsentStatements_grantsAndSanityChecksEachOne() {
            UUID secondStatementRef = UUID.randomUUID();
            UUID secondConsentId = UUID.randomUUID();
            cmd = new CreateApprenticeRegistrationCmd(
                    cmd.firstName(), cmd.lastName(), cmd.email(), cmd.organizationRef(), cmd.phoneNumbers(),
                    cmd.educationLineRef(), cmd.username(), cmd.status(),
                    List.of(new ConsentStatement(CONSENT_STATEMENT_REF, ConsentStatus.ACTIVE),
                            new ConsentStatement(secondStatementRef, ConsentStatus.ACTIVE)));

            stubOrganizationAndEducationLineOk();
            stubPersonOk();
            stubLoginOk();
            stubApprenticeOk();
            stubAuditlogOk();

            when(consentSagas.grant(any(GrantConsentCmd.class)))
                    .thenReturn(new ConsentResponse(CONSENT_ID, PERSON_ID, "ACTIVE", CONSENT_STATEMENT_REF, "T&C", ConsentPurpose.REQUIRED_SERVICE, ConsentType.REQUIRED, true))
                    .thenReturn(new ConsentResponse(secondConsentId, PERSON_ID, "ACTIVE", secondStatementRef, "Notifications", ConsentPurpose.COMMUNICATION, ConsentType.OPTIONAL, true));
            when(consents.getById(CONSENT_ID)).thenReturn(
                    new ConsentResponse(CONSENT_ID, PERSON_ID, "ACTIVE", CONSENT_STATEMENT_REF, "T&C", ConsentPurpose.REQUIRED_SERVICE, ConsentType.REQUIRED, true));
            when(consents.getById(secondConsentId)).thenReturn(
                    new ConsentResponse(secondConsentId, PERSON_ID, "ACTIVE", secondStatementRef, "Notifications", ConsentPurpose.COMMUNICATION, ConsentType.OPTIONAL, true));

            CreatedApprenticeResponse result = service.registerApprentice(cmd);

            assertEquals(APPRENTICE_ID, result.apprenticeId());
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

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(cmd));
            assertEquals("organization.not.found", ex.getMessage());
            verify(persons, never()).create(any());
        }

        @Test
        void educationLineNotFound_throwsConflictExceptionWithNoSideEffects() {
            when(organizations.getById(ORG_REF)).thenReturn(new OrganisationResponse(ORG_REF, "Zealand", "12345678"));
            when(educationLines.getById(EDUCATION_LINE_REF)).thenReturn(null);

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(cmd));
            assertEquals("educationline.not.found", ex.getMessage());
            verify(persons, never()).create(any());
        }
    }

    // ---- person step -------------------------------------------------------

    @Nested
    class PersonStep {

        @Test
        void creationFails_throwsConflictException_noCompensation() {
            stubOrganizationAndEducationLineOk();
            when(persons.create(any())).thenThrow(new RuntimeException("boom"));

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(cmd));
            assertEquals("person.not.created", ex.getMessage());
            verify(persons, never()).compensate(any(), any(), any());
        }

        @Test
        void sanityCheckReturnsNull_compensatesPerson() {
            stubOrganizationAndEducationLineOk();
            when(persons.create(any())).thenReturn(PERSON_ID);
            when(persons.getById(PERSON_ID)).thenReturn(null);
            when(persons.compensate(eq(PERSON_ID), any(), any())).thenReturn(ok());

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(cmd));
            assertEquals("person.not.found", ex.getMessage());
            verify(persons).compensate(eq(PERSON_ID), any(), any());
        }

        @Test
        void sanityCheckThrowsNotFound_compensatesPerson() {
            stubOrganizationAndEducationLineOk();
            when(persons.create(any())).thenReturn(PERSON_ID);
            when(persons.getById(PERSON_ID)).thenThrow(new NotFoundException("person.notfound", java.util.Map.of()));
            when(persons.compensate(eq(PERSON_ID), any(), any())).thenReturn(ok());

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(cmd));
            assertEquals("person.not.found", ex.getMessage());
            verify(persons).compensate(eq(PERSON_ID), any(), any());
        }
    }

    // ---- login step ---------------------------------------------------------

    @Nested
    class LoginStep {

        @Test
        void creationFails_compensatesPersonOnly() {
            stubOrganizationAndEducationLineOk();
            stubPersonOk();
            when(logins.create(any())).thenThrow(new RuntimeException("boom"));
            when(persons.compensate(any(), any(), any())).thenReturn(ok());

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(cmd));
            assertEquals("login.not.created", ex.getMessage());
            verify(persons).compensate(eq(PERSON_ID), any(), any());
            verify(logins, never()).compensate(any(), any(), any());
        }

        @Test
        void sanityCheckReturnsNull_compensatesLoginAndPerson() {
            stubOrganizationAndEducationLineOk();
            stubPersonOk();
            when(logins.create(any())).thenReturn(new ResponseLoginCreated(LOGIN_ID, "pw", null));
            when(logins.getById(LOGIN_ID)).thenReturn(null);
            stubAllCompensationsOk();

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(cmd));
            assertEquals("login.not.found", ex.getMessage());
            verify(logins).compensate(eq(LOGIN_ID), any(), any());
            verify(persons).compensate(eq(PERSON_ID), any(), any());
        }
    }

    // ---- apprentice step ------------------------------------------------------

    @Nested
    class ApprenticeStep {

        @Test
        void creationFails_compensatesLoginAndPerson() {
            stubOrganizationAndEducationLineOk();
            stubPersonOk();
            stubLoginOk();
            when(apprentices.create(any())).thenThrow(new RuntimeException("boom"));
            stubAllCompensationsOk();

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(cmd));
            assertEquals("apprentice.not.created", ex.getMessage());
            verify(logins).compensate(eq(LOGIN_ID), any(), any());
            verify(persons).compensate(eq(PERSON_ID), any(), any());
            verify(apprentices, never()).compensate(any(), any(), any());
        }

        @Test
        void sanityCheckReturnsNull_compensatesApprenticeLoginAndPerson() {
            stubOrganizationAndEducationLineOk();
            stubPersonOk();
            stubLoginOk();
            when(apprentices.create(any())).thenReturn(APPRENTICE_ID);
            when(apprentices.getById(APPRENTICE_ID)).thenReturn(null);
            stubAllCompensationsOk();

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(cmd));
            assertEquals("apprentice.not.found", ex.getMessage());
            verify(apprentices).compensate(eq(APPRENTICE_ID), any(), any());
            verify(logins).compensate(eq(LOGIN_ID), any(), any());
            verify(persons).compensate(eq(PERSON_ID), any(), any());
        }
    }

    // ---- consent step (grant via consentSagas, read via consents) --------

    @Nested
    class ConsentStep {

        @Test
        void grantFails_compensatesApprenticeLoginAndPerson() {
            stubOrganizationAndEducationLineOk();
            stubPersonOk();
            stubLoginOk();
            stubApprenticeOk();
            when(consentSagas.grant(any(GrantConsentCmd.class))).thenThrow(new RuntimeException("boom"));
            stubAllCompensationsOk();

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(cmd));
            assertEquals("consent.not.granted", ex.getMessage());
            verify(apprentices).compensate(eq(APPRENTICE_ID), any(), any());
            verify(logins).compensate(eq(LOGIN_ID), any(), any());
            verify(persons).compensate(eq(PERSON_ID), any(), any());
            // Nothing was granted before the (only) grant() call threw,
            // so there is nothing to compensate on the consent side.
            verify(consentSagas, never()).compensateConsent(any(), any(), any());
        }

        @Test
        void secondGrantFailsAfterFirstSucceeded_compensatesTheAlreadyGrantedConsentToo() {
            // Regression test: registerApprentice's grant-failure catch
            // was previously missing consentIds.forEach(this::compensateConsent),
            // present in registerInstructor's equivalent block. With a
            // single consent statement that gap never surfaced — nothing
            // is in consentIds yet when the only grant() call fails. This
            // exercises the case where it does: statement 1 grants
            // successfully, statement 2's grant() throws, and the
            // already-granted first consent must not be left orphaned.
            UUID secondStatementRef = UUID.randomUUID();
            cmd = new CreateApprenticeRegistrationCmd(
                    cmd.firstName(), cmd.lastName(), cmd.email(), cmd.organizationRef(), cmd.phoneNumbers(),
                    cmd.educationLineRef(), cmd.username(), cmd.status(),
                    List.of(new ConsentStatement(CONSENT_STATEMENT_REF, ConsentStatus.ACTIVE),
                            new ConsentStatement(secondStatementRef, ConsentStatus.ACTIVE)));

            stubOrganizationAndEducationLineOk();
            stubPersonOk();
            stubLoginOk();
            stubApprenticeOk();
            when(consentSagas.grant(any(GrantConsentCmd.class)))
                    .thenReturn(new ConsentResponse(CONSENT_ID, PERSON_ID, "ACTIVE", CONSENT_STATEMENT_REF, "T&C", ConsentPurpose.REQUIRED_SERVICE, ConsentType.REQUIRED, true))
                    .thenThrow(new RuntimeException("boom"));
            stubAllCompensationsOk();

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(cmd));
            assertEquals("consent.not.granted", ex.getMessage());
            // The consent from the FIRST (successful) grant call must be
            // compensated even though the overall step failed.
            verify(consentSagas).compensateConsent(eq(CONSENT_ID), any(), any());
            verify(apprentices).compensate(eq(APPRENTICE_ID), any(), any());
            verify(logins).compensate(eq(LOGIN_ID), any(), any());
            verify(persons).compensate(eq(PERSON_ID), any(), any());
        }

        @Test
        void sanityCheckReturnsNull_compensatesConsentApprenticeLoginAndPerson() {
            stubOrganizationAndEducationLineOk();
            stubPersonOk();
            stubLoginOk();
            stubApprenticeOk();
            when(consentSagas.grant(any(GrantConsentCmd.class))).thenReturn(
                    new ConsentResponse(CONSENT_ID, PERSON_ID, "ACTIVE", CONSENT_STATEMENT_REF, "T&C", ConsentPurpose.REQUIRED_SERVICE, ConsentType.REQUIRED, true));
            when(consents.getById(CONSENT_ID)).thenReturn(null);
            stubAllCompensationsOk();

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(cmd));
            assertEquals("consent.not.found", ex.getMessage());
            // Compensation goes through consentSagas, not consents — the
            // write-side port owns compensation, matching how grant() is
            // also on consentSagas rather than consents.
            verify(consentSagas).compensateConsent(eq(CONSENT_ID), any(), any());
            verify(apprentices).compensate(eq(APPRENTICE_ID), any(), any());
        }

        @Test
        void sanityCheckThrowsNotFound_compensatesConsentApprenticeLoginAndPerson() {
            stubOrganizationAndEducationLineOk();
            stubPersonOk();
            stubLoginOk();
            stubApprenticeOk();
            when(consentSagas.grant(any(GrantConsentCmd.class))).thenReturn(
                    new ConsentResponse(CONSENT_ID, PERSON_ID, "ACTIVE", CONSENT_STATEMENT_REF, "T&C", ConsentPurpose.REQUIRED_SERVICE, ConsentType.REQUIRED, true));
            when(consents.getById(CONSENT_ID)).thenThrow(new NotFoundException("consent.notfound", java.util.Map.of()));
            stubAllCompensationsOk();

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(cmd));
            assertEquals("consent.not.found", ex.getMessage());
            verify(consentSagas).compensateConsent(eq(CONSENT_ID), any(), any());
        }
    }

    // ---- audit log step -------------------------------------------------------

    @Nested
    class AuditlogStep {

        @Test
        void creationFails_compensatesConsentApprenticeLoginAndPerson() {
            stubOrganizationAndEducationLineOk();
            stubPersonOk();
            stubLoginOk();
            stubApprenticeOk();
            stubConsentGrantOk();
            when(auditlogs.create(any())).thenThrow(new RuntimeException("boom"));
            stubAllCompensationsOk();

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(cmd));
            assertEquals("auditlog.not.created", ex.getMessage());
            verify(consentSagas).compensateConsent(eq(CONSENT_ID), any(), any());
            verify(apprentices).compensate(eq(APPRENTICE_ID), any(), any());
            verify(auditlogs, never()).compensate(any(), any(), any());
        }

        @Test
        void sanityCheckReturnsNull_compensatesEverything() {
            stubOrganizationAndEducationLineOk();
            stubPersonOk();
            stubLoginOk();
            stubApprenticeOk();
            stubConsentGrantOk();
            when(auditlogs.create(any())).thenReturn(AUDITLOG_ID);
            when(auditlogs.getById(AUDITLOG_ID)).thenReturn(null);
            stubAllCompensationsOk();

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(cmd));
            assertEquals("auditlog.not.found", ex.getMessage());
            verify(auditlogs).compensate(eq(AUDITLOG_ID), any(), any());
            verify(consentSagas).compensateConsent(eq(CONSENT_ID), any(), any());
            verify(apprentices).compensate(eq(APPRENTICE_ID), any(), any());
            verify(logins).compensate(eq(LOGIN_ID), any(), any());
            verify(persons).compensate(eq(PERSON_ID), any(), any());
        }
    }
}