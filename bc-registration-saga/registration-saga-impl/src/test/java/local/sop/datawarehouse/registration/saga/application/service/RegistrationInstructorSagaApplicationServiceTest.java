package local.sop.datawarehouse.registration.saga.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentStatus;
import local.sop.common.libs.sharedkernel.enums.ConsentType;
import local.sop.common.libs.sharedkernel.enums.PhoneUserType;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.registration.saga.application.api.dto.CreateInstructorRegistrationCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.auditlog.AuditlogResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.auditlog.CreateAuditlogCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.consent.ConsentResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.consent.ConsentStatementResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.consent.GrantConsentCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.instructor.CreateInstructorCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.instructor.CreatedInstructorResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.instructor.InstructorResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.login.CreateLoginCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.login.LoginResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.organization.OrganisationResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.person.CreatePersonCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.person.CreatePhoneNumberCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.person.PersonResponse;
import local.sop.datawarehouse.registration.saga.application.infrastructure.response.ResponseLoginCreated;
import local.sop.datawarehouse.registration.saga.application.ports.out.apprentice.ApprenticePort;
import local.sop.datawarehouse.registration.saga.application.ports.out.auditlog.AuditlogPort;
import local.sop.datawarehouse.registration.saga.application.ports.out.consent.ConsentPort;
import local.sop.datawarehouse.registration.saga.application.ports.out.educationline.EducationLinePort;
import local.sop.datawarehouse.registration.saga.application.ports.out.instructor.InstructorPort;
import local.sop.datawarehouse.registration.saga.application.ports.out.login.LoginPort;
import local.sop.datawarehouse.registration.saga.application.ports.out.organization.OrganizationPort;
import local.sop.datawarehouse.registration.saga.application.ports.out.person.PersonPort;

@ExtendWith(MockitoExtension.class)
class RegistrationInstructorSagaApplicationServiceTest {

        @Mock
        private AuditlogPort auditlogs;

        @Mock
        private ApprenticePort apprentices;

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

        // Shared test fixtures for apprentice registration
        private UUID personId;
        private UUID loginId;
        private UUID instructorId;
        private UUID auditlogId;
        private UUID organizationId;
        private UUID consentStatementId;
        private List<CreatePhoneNumberCmd> phoneCmd;

        private CreateInstructorRegistrationCmd instructorCmd;

        private ResponseLoginCreated loginCreatedResponse;
        private ResponseCompensated compensatedResponse;

        @BeforeEach
        void setUp() {
                service = new RegistrationSagaApplicationService(apprentices, auditlogs, educationLines, consents,
                                instructors,
                                logins, organizations, persons);

                // Apprentice registration fixtures
                personId = UUID.randomUUID();
                loginId = UUID.randomUUID();
                auditlogId = UUID.randomUUID();
                organizationId = UUID.randomUUID();
                consentStatementId = UUID.randomUUID();
                loginCreatedResponse = new ResponseLoginCreated(loginId, "password", LocalDateTime.now());
                phoneCmd = List.of(new CreatePhoneNumberCmd(PhoneUserType.SELF, "MOBILE"));

                instructorId = UUID.randomUUID();

                instructorCmd = new CreateInstructorRegistrationCmd(
                                "Kris",
                                "K",
                                "kkri@zbc.dk",
                                organizationId,
                                phoneCmd,
                                "kkri",
                                "ACTIVE",
                                consentStatementId,
                                ConsentPurpose.REQUIRED_SERVICE,
                                ConsentType.REQUIRED,
                                ConsentStatus.ACTIVE);

                compensatedResponse = mock(ResponseCompensated.class);
        }

        @Test
        void registerInstructor_shouldReturnCreatedInstructorResponse_whenAllStepsSucceed() {
                // Setup - Verify dependencies exist
                when(organizations.getById(instructorCmd.organizationRef()))
                                .thenReturn(mock(OrganisationResponse.class));
                when(consents.getConsentStatementById(instructorCmd.consentStatementId()))
                                .thenReturn(mock(ConsentStatementResponse.class));

                when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);
                when(persons.getById(personId)).thenReturn(mock(PersonResponse.class));
                when(logins.create(any(CreateLoginCmd.class))).thenReturn(loginCreatedResponse);
                when(logins.getById(loginId)).thenReturn(mock(LoginResponse.class));
                when(instructors.create(any(CreateInstructorCmd.class))).thenReturn(instructorId);
                when(instructors.getById(instructorId)).thenReturn(mock(InstructorResponse.class));
                when(consents.grant(any(GrantConsentCmd.class))).thenReturn(UUID.randomUUID());
                when(consents.getById(any(UUID.class))).thenReturn(mock(ConsentResponse.class));
                when(auditlogs.create(any(CreateAuditlogCmd.class))).thenReturn(auditlogId);
                when(auditlogs.getById(auditlogId)).thenReturn(mock(AuditlogResponse.class));

                // Execute
                CreatedInstructorResponse result = service.registerInstructor(instructorCmd);

                // Verify
                assertNotNull(result);
                assertEquals(instructorId, result.id());
        }

        // Negative test cases
        @Test
        void registerInstructor_shouldThrowConflictException_whenOrganizationDoesNotExist() {

                // Setup - Organization does not exist
                when(organizations.getById(instructorCmd.organizationRef())).thenReturn(null);

                // Execute & Verify
                assertThrows(Exception.class, () -> service.registerInstructor(instructorCmd));
                verify(consents, never()).getConsentStatementById(any());
                verify(persons, never()).create(any());
        }

        @Test
        void registerInstructor_shouldThrowConflictException_whenPersonCreationFails() {
                when(organizations.getById(instructorCmd.organizationRef()))
                                .thenReturn(mock(OrganisationResponse.class));

                when(consents.getConsentStatementById(instructorCmd.consentStatementId()))
                                .thenReturn(mock(ConsentStatementResponse.class));

                when(persons.create(any(CreatePersonCmd.class)))
                                .thenThrow(new RuntimeException("Person service unavailable"));

                // when
                ConflictException ex = assertThrows(
                                ConflictException.class,
                                () -> service.registerInstructor(instructorCmd));

                // then
                assertEquals("person.not.created", ex.getMessage());

        }

        @Test
        void registerInstructor_shouldThrowConflictException_whenPersonRetrievalFails() {
                when(organizations.getById(instructorCmd.organizationRef()))
                                .thenReturn(mock(OrganisationResponse.class));

                when(consents.getConsentStatementById(instructorCmd.consentStatementId()))
                                .thenReturn(mock(ConsentStatementResponse.class));

                when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);

                when(persons.getById(personId))
                                .thenThrow(new RuntimeException("Person service unavailable"));

                when(persons.compensate(any(), any(), any()))
                                .thenReturn(compensatedResponse);

                // when
                ConflictException ex = assertThrows(
                                ConflictException.class,
                                () -> service.registerInstructor(instructorCmd));

                // then
                assertEquals("person.read.failed", ex.getMessage());

        }

        @Test
        void registerInstructor_shouldThrowConflictException_whenPersonDoesNotExist() {
                when(organizations.getById(instructorCmd.organizationRef()))
                                .thenReturn(mock(OrganisationResponse.class));

                when(consents.getConsentStatementById(instructorCmd.consentStatementId()))
                                .thenReturn(mock(ConsentStatementResponse.class));

                when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);

                when(persons.getById(personId))
                                .thenReturn(null);

                when(persons.compensate(any(), any(), any()))
                                .thenReturn(compensatedResponse);

                // when
                ConflictException ex = assertThrows(
                                ConflictException.class,
                                () -> service.registerInstructor(instructorCmd));

                // then
                assertEquals("person.not.found", ex.getMessage());

        }

        @Test
        void registerInstructor_shouldThrowConflictException_whenLoginCreationFails() {

                when(organizations.getById(instructorCmd.organizationRef()))
                                .thenReturn(mock(OrganisationResponse.class));

                when(consents.getConsentStatementById(instructorCmd.consentStatementId()))
                                .thenReturn(mock(ConsentStatementResponse.class));

                when(persons.create(any(CreatePersonCmd.class)))
                                .thenReturn(personId);

                when(persons.getById(personId))
                                .thenReturn(mock(PersonResponse.class));

                when(persons.compensate(any(), any(), any()))
                                .thenReturn(compensatedResponse);

                // when
                ConflictException ex = assertThrows(
                                ConflictException.class,
                                () -> service.registerInstructor(instructorCmd));

                // then
                assertEquals("login.not.created", ex.getMessage());
        }

        @Test
        void registerInstructor_shouldThrowConflictException_whenLoginDoesNotExist() {

                // given
                when(organizations.getById(instructorCmd.organizationRef()))
                                .thenReturn(mock(OrganisationResponse.class));

                when(consents.getConsentStatementById(instructorCmd.consentStatementId()))
                                .thenReturn(mock(ConsentStatementResponse.class));

                when(persons.create(any(CreatePersonCmd.class)))
                                .thenReturn(personId);

                when(persons.getById(personId))
                                .thenReturn(mock(PersonResponse.class));

                when(logins.create(any(CreateLoginCmd.class)))
                                .thenReturn(loginCreatedResponse);

                when(logins.getById(loginId))
                                .thenReturn(null);

                when(logins.compensate(any(), any(), any()))
                                .thenReturn(compensatedResponse);

                when(persons.compensate(any(), any(), any()))
                                .thenReturn(compensatedResponse);

                // when
                ConflictException ex = assertThrows(
                                ConflictException.class,
                                () -> service.registerInstructor(instructorCmd));

                // then
                assertEquals("login.not.found", ex.getMessage());

                // verify flow reached expected steps
                verify(organizations).getById(instructorCmd.organizationRef());
                verify(consents).getConsentStatementById(instructorCmd.consentStatementId());

                verify(persons).create(any(CreatePersonCmd.class));
                verify(persons).getById(personId);

                verify(logins).create(any(CreateLoginCmd.class));
                verify(logins).getById(loginId);

                // verify compensation happened
                verify(logins).compensate(any(), any(), any());
                verify(persons).compensate(any(), any(), any());
        }

        @Test
        void registerInstructor_shouldThrowConflictException_whenLoginRetrievalFails() {

                // given
                when(organizations.getById(instructorCmd.organizationRef()))
                                .thenReturn(mock(OrganisationResponse.class));

                when(consents.getConsentStatementById(instructorCmd.consentStatementId()))
                                .thenReturn(mock(ConsentStatementResponse.class));

                when(persons.create(any(CreatePersonCmd.class)))
                                .thenReturn(personId);

                when(persons.getById(personId))
                                .thenReturn(mock(PersonResponse.class));

                when(logins.create(any(CreateLoginCmd.class)))
                                .thenReturn(loginCreatedResponse);

                when(logins.getById(loginId))
                                .thenThrow(new RuntimeException("Login service unavailable"));

                when(logins.compensate(any(), any(), any()))
                                .thenReturn(compensatedResponse);

                when(persons.compensate(any(), any(), any()))
                                .thenReturn(compensatedResponse);

                // when
                ConflictException ex = assertThrows(
                                ConflictException.class,
                                () -> service.registerInstructor(instructorCmd));

                // then
                assertEquals("login.read.failed", ex.getMessage());

                // verify flow reached expected steps
                verify(organizations).getById(instructorCmd.organizationRef());
                verify(consents).getConsentStatementById(instructorCmd.consentStatementId());

                verify(persons).create(any(CreatePersonCmd.class));
                verify(persons).getById(personId);

                verify(logins).create(any(CreateLoginCmd.class));
                verify(logins).getById(loginId);

                // verify compensation happened
                verify(logins).compensate(any(), any(), any());
                verify(persons).compensate(any(), any(), any());
        }

        @Test
        void registerInstructor_shouldThrowConflictException_whenInstructorCreationFails() {

                when(organizations.getById(instructorCmd.organizationRef()))
                                .thenReturn(mock(OrganisationResponse.class));

                when(consents.getConsentStatementById(instructorCmd.consentStatementId()))
                                .thenReturn(mock(ConsentStatementResponse.class));

                when(persons.create(any(CreatePersonCmd.class)))
                                .thenReturn(personId);

                when(persons.getById(personId))
                                .thenReturn(mock(PersonResponse.class));

                when(logins.create(any(CreateLoginCmd.class)))
                                .thenReturn(loginCreatedResponse);

                when(logins.getById(loginId))
                                .thenReturn(mock(LoginResponse.class));

                when(instructors.create(any(CreateInstructorCmd.class)))
                                .thenThrow(new RuntimeException("Instructor service unavailable"));

                when(logins.compensate(any(), any(), any()))
                                .thenReturn(compensatedResponse);

                when(persons.compensate(any(), any(), any()))
                                .thenReturn(compensatedResponse);

                // when
                ConflictException ex = assertThrows(
                                ConflictException.class,
                                () -> service.registerInstructor(instructorCmd));

                // then
                assertEquals("instructor.not.created", ex.getMessage());
        }

        @Test
        void registerInstructor_shouldThrowConflictException_whenInstructorRetrievalFails() {

                when(organizations.getById(instructorCmd.organizationRef()))
                                .thenReturn(mock(OrganisationResponse.class));

                when(consents.getConsentStatementById(instructorCmd.consentStatementId()))
                                .thenReturn(mock(ConsentStatementResponse.class));

                when(persons.create(any(CreatePersonCmd.class)))
                                .thenReturn(personId);

                when(persons.getById(personId))
                                .thenReturn(mock(PersonResponse.class));

                when(logins.create(any(CreateLoginCmd.class)))
                                .thenReturn(loginCreatedResponse);

                when(logins.getById(loginId))
                                .thenReturn(mock(LoginResponse.class));
                when(instructors.create(any(CreateInstructorCmd.class)))
                                .thenReturn(instructorId);

                when(instructors.getById(instructorId))
                                .thenThrow(new RuntimeException("Instructor service unavailable"));

                when(instructors.compensate(any(), any(), any()))
                                .thenReturn(compensatedResponse);

                when(logins.compensate(any(), any(), any()))
                                .thenReturn(compensatedResponse);

                when(persons.compensate(any(), any(), any()))
                                .thenReturn(compensatedResponse);

                // when
                ConflictException ex = assertThrows(
                                ConflictException.class,
                                () -> service.registerInstructor(instructorCmd));

                // then
                assertEquals("instructor.read.failed", ex.getMessage());
        }

        @Test
        void registerInstructor_shouldThrowConflictException_whenInstructorDoesNotExist() {
                // Setup - Instructor creation fails
                when(organizations.getById(instructorCmd.organizationRef()))
                                .thenReturn(mock(OrganisationResponse.class));

                when(consents.getConsentStatementById(instructorCmd.consentStatementId()))
                                .thenReturn(mock(ConsentStatementResponse.class));

                when(persons.create(any(CreatePersonCmd.class)))
                                .thenReturn(personId);

                when(persons.getById(personId))
                                .thenReturn(mock(PersonResponse.class));

                when(logins.create(any(CreateLoginCmd.class)))
                                .thenReturn(loginCreatedResponse);

                when(logins.getById(loginId))
                                .thenReturn(mock(LoginResponse.class));
                when(instructors.create(any(CreateInstructorCmd.class)))
                                .thenReturn(instructorId);

                when(instructors.getById(instructorId))
                                .thenReturn(null);

                when(instructors.compensate(any(), any(), any()))
                                .thenReturn(compensatedResponse);
                when(logins.compensate(any(), any(), any()))
                                .thenReturn(compensatedResponse);
                when(persons.compensate(any(), any(), any()))
                                .thenReturn(compensatedResponse);

                ConflictException ex = assertThrows(
                                ConflictException.class,
                                () -> service.registerInstructor(instructorCmd));

                assertEquals("instructor.not.found", ex.getMessage());
        }

        @Test
        void registerInstructor_shouldThrowConflictException_whenConsentCreationFails() {
                when(organizations.getById(instructorCmd.organizationRef()))
                                .thenReturn(mock(OrganisationResponse.class));
                when(consents.getConsentStatementById(instructorCmd.consentStatementId()))
                                .thenReturn(mock(ConsentStatementResponse.class));

                when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);

                when(persons.getById(personId)).thenReturn(mock(PersonResponse.class));

                when(logins.create(any(CreateLoginCmd.class)))
                                .thenReturn(loginCreatedResponse);

                when(logins.getById(loginId)).thenReturn(mock(LoginResponse.class));

                when(instructors.create(any(CreateInstructorCmd.class)))
                                .thenReturn(instructorId);
                when(instructors.getById(instructorId))
                                .thenReturn(mock(InstructorResponse.class));
                when(consents.grant(any(GrantConsentCmd.class)))
                                .thenThrow(new RuntimeException("consent service unavailable"));

                when(instructors.compensate(eq(instructorId), any(), any()))
                                .thenReturn(compensatedResponse);
                when(logins.compensate(eq(loginId), any(), any())).thenReturn(compensatedResponse);
                when(persons.compensate(eq(personId), any(), any())).thenReturn(compensatedResponse);

                ConflictException ex = assertThrows(ConflictException.class,
                                () -> service.registerInstructor(instructorCmd));

                assertEquals("consent.not.granted", ex.messageKey());
        }

        @Test
        void registerInstructor_shouldThrowConflictException_whenConsentRetrievalFails() {
                when(organizations.getById(instructorCmd.organizationRef()))
                                .thenReturn(mock(OrganisationResponse.class));
                when(consents.getConsentStatementById(instructorCmd.consentStatementId()))
                                .thenReturn(mock(ConsentStatementResponse.class));

                when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);

                when(persons.getById(personId)).thenReturn(mock(PersonResponse.class));

                when(logins.create(any(CreateLoginCmd.class)))
                                .thenReturn(loginCreatedResponse);

                when(logins.getById(loginId)).thenReturn(mock(LoginResponse.class));

                when(instructors.create(any(CreateInstructorCmd.class)))
                                .thenReturn(instructorId);

                when(instructors.getById(instructorId))
                                .thenReturn(mock(InstructorResponse.class));
                when(consents.grant(any(GrantConsentCmd.class)))
                                .thenReturn(consentStatementId);
                when(consents.getById(consentStatementId))
                                .thenThrow(new RuntimeException("consent service unavailable"));

                when(consents.compensate(any(), any(), any())).thenReturn(compensatedResponse);
                when(instructors.compensate(eq(instructorId), any(), any()))
                                .thenReturn(compensatedResponse);
                when(logins.compensate(eq(loginId), any(), any())).thenReturn(compensatedResponse);
                when(persons.compensate(eq(personId), any(), any())).thenReturn(compensatedResponse);

                ConflictException ex = assertThrows(ConflictException.class,
                                () -> service.registerInstructor(instructorCmd));

                assertEquals("consent.read.failed", ex.messageKey());
        }

        @Test
        void registerInstructor_shouldThrowConflictException_whenConsentDoesNotExist() {
                when(organizations.getById(instructorCmd.organizationRef()))
                                .thenReturn(mock(OrganisationResponse.class));
                when(consents.getConsentStatementById(instructorCmd.consentStatementId()))
                                .thenReturn(mock(ConsentStatementResponse.class));

                when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);

                when(persons.getById(personId)).thenReturn(mock(PersonResponse.class));

                when(logins.create(any(CreateLoginCmd.class)))
                                .thenReturn(loginCreatedResponse);

                when(logins.getById(loginId)).thenReturn(mock(LoginResponse.class));

                when(instructors.create(any(CreateInstructorCmd.class)))
                                .thenReturn(instructorId);

                when(instructors.getById(instructorId))
                                .thenReturn(mock(InstructorResponse.class));
                when(consents.grant(any(GrantConsentCmd.class)))
                                .thenReturn(consentStatementId);
                when(consents.getById(consentStatementId)).thenReturn(null);

                when(consents.compensate(any(), any(), any())).thenReturn(compensatedResponse);
                when(instructors.compensate(eq(instructorId), any(), any()))
                                .thenReturn(compensatedResponse);
                when(logins.compensate(eq(loginId), any(), any())).thenReturn(compensatedResponse);
                when(persons.compensate(eq(personId), any(), any())).thenReturn(compensatedResponse);

                ConflictException ex = assertThrows(ConflictException.class,
                                () -> service.registerInstructor(instructorCmd));

                assertEquals("consent.not.found", ex.messageKey());

        }

        @Test
        void registerInstructor_shouldThrowConflictException_whenAuditLogCreationFails() {
                when(organizations.getById(instructorCmd.organizationRef()))
                                .thenReturn(mock(OrganisationResponse.class));
                when(consents.getConsentStatementById(instructorCmd.consentStatementId()))
                                .thenReturn(mock(ConsentStatementResponse.class));

                when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);
                when(persons.getById(personId)).thenReturn(mock(PersonResponse.class));
                when(logins.create(any(CreateLoginCmd.class)))
                                .thenReturn(loginCreatedResponse);
                when(logins.getById(loginId)).thenReturn(mock(LoginResponse.class));
                when(instructors.create(any(CreateInstructorCmd.class)))
                                .thenReturn(instructorId);
                when(instructors.getById(instructorId))
                                .thenReturn(mock(InstructorResponse.class));
                when(consents.grant(any(GrantConsentCmd.class)))
                                .thenReturn(consentStatementId);
                when(consents.getById(consentStatementId))
                                .thenReturn(mock(ConsentResponse.class));

                when(auditlogs.create(any(CreateAuditlogCmd.class))).thenThrow(
                                new RuntimeException("auditlog service unavailable"));

                when(consents.compensate(any(), any(), any())).thenReturn(compensatedResponse);
                when(instructors.compensate(eq(instructorId), any(), any()))
                                .thenReturn(compensatedResponse);
                when(logins.compensate(eq(loginId), any(), any())).thenReturn(compensatedResponse);
                when(persons.compensate(eq(personId), any(), any())).thenReturn(compensatedResponse);

                ConflictException ex = assertThrows(ConflictException.class,
                                () -> service.registerInstructor(instructorCmd));

                assertEquals("auditlog.not.created", ex.messageKey());
        }

        @Test
        void registerInstructor_shouldThrowConflictException_whenAuditLogRetrievalFails() {
                when(organizations.getById(instructorCmd.organizationRef()))
                                .thenReturn(mock(OrganisationResponse.class));
                when(consents.getConsentStatementById(instructorCmd.consentStatementId()))
                                .thenReturn(mock(ConsentStatementResponse.class));

                when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);
                when(persons.getById(personId)).thenReturn(mock(PersonResponse.class));
                when(logins.create(any(CreateLoginCmd.class)))
                                .thenReturn(loginCreatedResponse);
                when(logins.getById(loginId)).thenReturn(mock(LoginResponse.class));
                when(instructors.create(any(CreateInstructorCmd.class)))
                                .thenReturn(instructorId);
                when(instructors.getById(instructorId))
                                .thenReturn(mock(InstructorResponse.class));
                when(consents.grant(any(GrantConsentCmd.class)))
                                .thenReturn(consentStatementId);
                when(consents.getById(consentStatementId))
                                .thenReturn(mock(ConsentResponse.class));
                when(auditlogs.create(any(CreateAuditlogCmd.class))).thenReturn(auditlogId);

                when(auditlogs.getById(auditlogId)).thenThrow(
                                new RuntimeException("auditlog service unavailable"));

                when(auditlogs.compensate(any(), any(), any())).thenReturn(compensatedResponse);
                when(consents.compensate(any(), any(), any())).thenReturn(compensatedResponse);
                when(instructors.compensate(eq(instructorId), any(), any()))
                                .thenReturn(compensatedResponse);
                when(logins.compensate(eq(loginId), any(), any())).thenReturn(compensatedResponse);
                when(persons.compensate(eq(personId), any(), any())).thenReturn(compensatedResponse);

                ConflictException ex = assertThrows(ConflictException.class,
                                () -> service.registerInstructor(instructorCmd));

                assertEquals("auditlog.read.failed", ex.messageKey());
        }

        @Test
        void registerInstructor_shouldThrowConflictException_whenAuditLogDoesNotExist() {
                when(organizations.getById(instructorCmd.organizationRef()))
                                .thenReturn(mock(OrganisationResponse.class));
                when(consents.getConsentStatementById(instructorCmd.consentStatementId()))
                                .thenReturn(mock(ConsentStatementResponse.class));

                when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);
                when(persons.getById(personId)).thenReturn(mock(PersonResponse.class));
                when(logins.create(any(CreateLoginCmd.class)))
                                .thenReturn(loginCreatedResponse);
                when(logins.getById(loginId)).thenReturn(mock(LoginResponse.class));
                when(instructors.create(any(CreateInstructorCmd.class)))
                                .thenReturn(instructorId);
                when(instructors.getById(instructorId))
                                .thenReturn(mock(InstructorResponse.class));
                when(consents.grant(any(GrantConsentCmd.class)))
                                .thenReturn(consentStatementId);
                when(consents.getById(consentStatementId))
                                .thenReturn(mock(ConsentResponse.class));
                when(auditlogs.create(any(CreateAuditlogCmd.class))).thenReturn(auditlogId);

                when(auditlogs.getById(auditlogId)).thenReturn(null);

                when(auditlogs.compensate(any(), any(), any())).thenReturn(compensatedResponse);
                when(consents.compensate(any(), any(), any())).thenReturn(compensatedResponse);
                when(instructors.compensate(eq(instructorId), any(), any()))
                                .thenReturn(compensatedResponse);
                when(logins.compensate(eq(loginId), any(), any())).thenReturn(compensatedResponse);
                when(persons.compensate(eq(personId), any(), any())).thenReturn(compensatedResponse);

                ConflictException ex = assertThrows(ConflictException.class,
                                () -> service.registerInstructor(instructorCmd));

                assertEquals("auditlog.not.found", ex.messageKey());

        }

}
