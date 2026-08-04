package local.sop.sopinfo.registration.saga.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import org.springframework.beans.factory.annotation.Autowired;

import local.sop.sopinfo.registration.saga.application.api.dto.CreateApprenticeRegistrationCmd;
import local.sop.sopinfo.registration.saga.application.api.dto.educationline.EducationLineResponse;
import local.sop.sopinfo.registration.saga.application.api.dto.organization.OrganisationResponse;
import local.sop.sopinfo.registration.saga.application.api.dto.person.CreatePhoneNumberCmd;
import local.sop.sopinfo.registration.saga.application.api.dto.person.PhoneNumberResponse;
import local.sop.sopinfo.registration.saga.application.ports.out.apprentice.ApprenticePort;
import local.sop.sopinfo.registration.saga.application.ports.out.auditlog.AuditlogPort;
import local.sop.sopinfo.registration.saga.application.ports.out.consent.ConsentPort;
import local.sop.sopinfo.registration.saga.application.ports.out.educationline.EducationLinePort;
import local.sop.sopinfo.registration.saga.application.ports.out.instructor.InstructorPort;
import local.sop.sopinfo.registration.saga.application.ports.out.login.LoginPort;
import local.sop.sopinfo.registration.saga.application.ports.out.organization.OrganizationPort;
import local.sop.sopinfo.registration.saga.application.ports.out.person.PersonPort;
import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentStatus;
import local.sop.common.libs.sharedkernel.enums.ConsentType;
import local.sop.common.libs.sharedkernel.enums.PhoneUserType;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;

@ExtendWith(MockitoExtension.class)
class RegistrationDependenciesSagaApplicationTest {

    @Mock
    private ApprenticePort apprentices;

    @Mock
    private AuditlogPort auditlogs;

    @Mock
    private ConsentPort consents;

    @Mock
    private EducationLinePort educationLines;

    @Mock
    private InstructorPort instructors;

    @Mock
    private LoginPort logins;

    @Mock
    private OrganizationPort organizations;

    @Mock
    private PersonPort persons;

    @Autowired
    private RegistrationSagaApplicationService service;

    private UUID organizationId;
    private UUID educationLineId;
    private UUID consentStatementId;
    private List<CreatePhoneNumberCmd> phoneCmd;
    private CreateApprenticeRegistrationCmd apprenticeCmd;

    @BeforeEach
    void setUp() {
        service = new RegistrationSagaApplicationService(apprentices, auditlogs, educationLines, consents,
                instructors,
                logins, organizations, persons);

        organizationId = UUID.randomUUID();
        educationLineId = UUID.randomUUID();
        consentStatementId = UUID.randomUUID();
        phoneCmd = List.of(new CreatePhoneNumberCmd(PhoneUserType.SELF, "MOBILE"));

        apprenticeCmd = new CreateApprenticeRegistrationCmd(
                "Daniel",
                "S",
                "dani423j@zbc.dk",
                organizationId,
                phoneCmd,
                educationLineId,
                "dani423j",
                "ACTIVE",
                consentStatementId,
                ConsentPurpose.REQUIRED_SERVICE,
                ConsentType.REQUIRED,
                ConsentStatus.ACTIVE);

    }

    @Nested
    class DependencyVerification {

        @Test
        void registerApprentice_shouldThrowConflictException_whenOrganizationDoesNotExist() {
            when(organizations.getById(apprenticeCmd.organizationRef())).thenReturn(null);

            assertThrows(Exception.class, () -> service.registerApprentice(apprenticeCmd));

            verify(educationLines, never()).getById(any());
            verify(consents, never()).getConsentStatementById(any());
            verify(persons, never()).create(any());
        }

        @Test
        void registerApprentice_shouldThrowConflictException_whenOrganizationRetrievalFails() {
            when(organizations.getById(apprenticeCmd.organizationRef())).thenThrow(
                    new RuntimeException("organization service unavailable"));

            ConflictException ex = assertThrows(ConflictException.class,
                    () -> service.registerApprentice(apprenticeCmd));

            assertEquals("organization.read.failed", ex.messageKey());

            verify(educationLines, never()).getById(any());
            verify(consents, never()).getConsentStatementById(any());
            verify(persons, never()).create(any());
        }

        @Test
        void registerApprentice_shouldThrowConflictException_whenEducationLineDoesNotExist() {
            when(organizations.getById(apprenticeCmd.organizationRef()))
                    .thenReturn(mock(OrganisationResponse.class));
            when(educationLines.getById(apprenticeCmd.educationLineRef())).thenReturn(null);

            assertThrows(Exception.class, () -> service.registerApprentice(apprenticeCmd));

            verify(consents, never()).getConsentStatementById(any());
            verify(persons, never()).create(any());
        }

        @Test
        void registerApprentice_shouldThrowConflictException_whenEducationLineRetrievalFails() {
            when(organizations.getById(apprenticeCmd.organizationRef()))
                    .thenReturn(mock(OrganisationResponse.class));
            when(educationLines.getById(apprenticeCmd.educationLineRef())).thenThrow(
                    new RuntimeException("educationline service unavailable"));

            ConflictException ex = assertThrows(ConflictException.class,
                    () -> service.registerApprentice(apprenticeCmd));

            assertEquals("educationline.read.failed", ex.messageKey());

            verify(consents, never()).getConsentStatementById(any());
            verify(persons, never()).create(any());
        }

        @Test
        void registerApprentice_shouldThrowConflictException_whenConsentStatementDoesNotExist() {
            when(organizations.getById(apprenticeCmd.organizationRef()))
                    .thenReturn(mock(OrganisationResponse.class));
            when(educationLines.getById(apprenticeCmd.educationLineRef()))
                    .thenReturn(mock(EducationLineResponse.class));
            when(consents.getConsentStatementById(apprenticeCmd.consentStatementId()))
                    .thenReturn(null);

            assertThrows(Exception.class, () -> service.registerApprentice(apprenticeCmd));

            verify(persons, never()).create(any());
        }

        @Test
        void registerApprentice_shouldThrowConflictException_whenConsentStatementRetrievalFails() {
            when(organizations.getById(apprenticeCmd.organizationRef()))
                    .thenReturn(mock(OrganisationResponse.class));
            when(educationLines.getById(apprenticeCmd.educationLineRef()))
                    .thenReturn(mock(EducationLineResponse.class));
            when(consents.getConsentStatementById(apprenticeCmd.consentStatementId()))
                    .thenThrow(new RuntimeException(
                            "educationline service unavailable"));

            ConflictException ex = assertThrows(ConflictException.class,
                    () -> service.registerApprentice(apprenticeCmd));

            assertEquals("consentstatement.read.failed", ex.messageKey());

            verify(persons, never()).create(any());
        }

        @Test
        void registerApprentice_shouldNotCompensateAnyhing_whenDependenciesNotFound() {
            when(organizations.getById(apprenticeCmd.organizationRef())).thenReturn(null);

            assertThrows(Exception.class, () -> service.registerApprentice(apprenticeCmd));

            verify(persons, never()).compensate(any(), any(), any());
            verify(logins, never()).compensate(any(), any(), any());
            verify(apprentices, never()).compensate(any(), any(), any());
            verify(consents, never()).compensate(any(), any(), any());
            verify(auditlogs, never()).compensate(any(), any(), any());
        }

        @Test
        void shouldCreatePhoneNumberResponse() {
                UUID id = UUID.randomUUID();

                PhoneNumberResponse response =
                        new PhoneNumberResponse(id, PhoneUserType.SELF, "12345678");

                assertEquals(id, response.id());
                assertEquals(PhoneUserType.SELF, response.type());
                assertEquals("12345678", response.value());
        }
    }
}
