package local.sop.datawarehouse.registration.saga.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import local.sop.datawarehouse.sharedlib.enums.ConsentStatus;
import local.sop.common.libs.sharedkernel.enums.PhoneUserType;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.datawarehouse.registration.saga.application.api.dto.CreateApprenticeRegistrationCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.CreateInstructorRegistrationCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.organization.OrganisationResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.person.CreatePhoneNumberCmd;
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
 * Focused on the shared pre-flight dependency checks —
 * verifyOrganizationExists() (shared by both register flows) and
 * verifyEducationLineExists() (apprentice only) — going deeper on the
 * failure modes than the PreFlightVerification nested class in the two
 * flow-specific test files, which only covers the null-return case.
 * This file covers NotFoundException, an unexpected generic exception,
 * and ConflictException passthrough for both dependency checks, plus
 * confirms neither check has ANY side effect on the ports beyond the
 * organization/education-line lookup itself — these are pure
 * verification steps, nothing should be created or compensated before
 * they pass.
 */
@ExtendWith(MockitoExtension.class)
class RegistrationDependenciesSagaApplicationTest {

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
    private static final UUID CONSENT_STATEMENT_REF = UUID.randomUUID();

    private CreateApprenticeRegistrationCmd apprenticeCmd;
    private CreateInstructorRegistrationCmd instructorCmd;

    @BeforeEach
    void setUp() {
        service = new RegistrationSagaApplicationService(apprentices, auditlogs, educationLines, consentSagas,
                consents, instructors, logins, organizations, persons);

        apprenticeCmd = new CreateApprenticeRegistrationCmd(
                "Daniel", "S", "dani@example.com", ORG_REF,
                List.of(new CreatePhoneNumberCmd(PhoneUserType.SELF, "+4512345678")),
                EDUCATION_LINE_REF, "danis", "ACTIVE",
                List.of(new CreateApprenticeRegistrationCmd.ConsentStatement(CONSENT_STATEMENT_REF, ConsentStatus.ACTIVE)));

        instructorCmd = new CreateInstructorRegistrationCmd(
                "Kris", "K", "kris@example.com", ORG_REF,
                List.of(new CreatePhoneNumberCmd(PhoneUserType.SELF, "+4587654321")),
                "krisk", "ACTIVE",
                List.of(new CreateInstructorRegistrationCmd.ConsentStatement(CONSENT_STATEMENT_REF, ConsentStatus.ACTIVE)));
    }

    // ---- organization check — shared by both flows ------------------------

    @Nested
    class OrganizationCheck {

        @Test
        void returnsNull_forApprentice_throwsConflictExceptionAndCreatesNothing() {
            when(organizations.getById(ORG_REF)).thenReturn(null);

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(apprenticeCmd));
            assertEquals("organization.not.found", ex.getMessage());
            verify_nothingCreated();
        }

        @Test
        void returnsNull_forInstructor_throwsConflictExceptionAndCreatesNothing() {
            when(organizations.getById(ORG_REF)).thenReturn(null);

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerInstructor(instructorCmd));
            assertEquals("organization.not.found", ex.getMessage());
            verify_nothingCreated();
        }

        @Test
        void portThrowsNotFoundException_translatedToConflictException() {
            when(organizations.getById(ORG_REF)).thenThrow(new NotFoundException("organization.notfound", Map.of()));

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(apprenticeCmd));
            assertEquals("organization.not.found", ex.getMessage());
            verify_nothingCreated();
        }

        @Test
        void portThrowsUnexpectedException_translatedToReadFailedConflictException() {
            when(organizations.getById(ORG_REF)).thenThrow(new RuntimeException("connection refused"));

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(apprenticeCmd));
            assertEquals("organization.read.failed", ex.getMessage());
            verify_nothingCreated();
        }

        @Test
        void portThrowsConflictException_passedThroughUnwrapped() {
            ConflictException original = new ConflictException("organization.some.specific.conflict", Map.of("id", ORG_REF.toString()));
            when(organizations.getById(ORG_REF)).thenThrow(original);

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(apprenticeCmd));
            // The original exception's own message survives — it is NOT
            // wrapped into a generic "organization.not.found"/"read.failed".
            assertEquals("organization.some.specific.conflict", ex.getMessage());
            verify_nothingCreated();
        }

        private void verify_nothingCreated() {
            verify(persons, never()).create(any());
            verify(logins, never()).create(any());
            verify(apprentices, never()).create(any());
            verify(instructors, never()).create(any());
            verify(consentSagas, never()).grant(any());
            verify(auditlogs, never()).create(any());
        }
    }

    // ---- education line check — apprentice flow only -----------------------

    @Nested
    class EducationLineCheck {

        @BeforeEach
        void organizationAlwaysExists() {
            when(organizations.getById(ORG_REF)).thenReturn(new OrganisationResponse(ORG_REF, "Zealand", "12345678"));
        }

        @Test
        void returnsNull_throwsConflictExceptionAndCreatesNothing() {
            when(educationLines.getById(EDUCATION_LINE_REF)).thenReturn(null);

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(apprenticeCmd));
            assertEquals("educationline.not.found", ex.getMessage());
            verify_nothingCreated();
        }

        @Test
        void portThrowsNotFoundException_translatedToConflictException() {
            when(educationLines.getById(EDUCATION_LINE_REF)).thenThrow(new NotFoundException("educationline.notfound", Map.of()));

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(apprenticeCmd));
            assertEquals("educationline.not.found", ex.getMessage());
            verify_nothingCreated();
        }

        @Test
        void portThrowsUnexpectedException_translatedToReadFailedConflictException() {
            when(educationLines.getById(EDUCATION_LINE_REF)).thenThrow(new RuntimeException("timeout"));

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(apprenticeCmd));
            assertEquals("educationline.read.failed", ex.getMessage());
            verify_nothingCreated();
        }

        @Test
        void portThrowsConflictException_passedThroughUnwrapped() {
            ConflictException original = new ConflictException("educationline.some.specific.conflict", Map.of("id", EDUCATION_LINE_REF.toString()));
            when(educationLines.getById(EDUCATION_LINE_REF)).thenThrow(original);

            ConflictException ex = assertThrows(ConflictException.class, () -> service.registerApprentice(apprenticeCmd));
            assertEquals("educationline.some.specific.conflict", ex.getMessage());
            verify_nothingCreated();
        }

        @Test
        void checkedAfterOrganization_notBeforeIt() {
            // Organization check runs first — if it fails, education
            // line should never even be looked up.
            when(organizations.getById(ORG_REF)).thenReturn(null);

            assertThrows(ConflictException.class, () -> service.registerApprentice(apprenticeCmd));
            verify(educationLines, never()).getById(any());
        }

        private void verify_nothingCreated() {
            verify(persons, never()).create(any());
            verify(logins, never()).create(any());
            verify(apprentices, never()).create(any());
            verify(consentSagas, never()).grant(any());
            verify(auditlogs, never()).create(any());
        }
    }
}