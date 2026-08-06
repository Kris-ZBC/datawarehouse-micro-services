package local.sop.datawarehouse.registration.saga.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.registration.saga.application.api.dto.CreateApprenticeRegistrationCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.apprentice.ApprenticeResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.apprentice.CreateApprenticeCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.apprentice.CreatedApprenticeResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.auditlog.AuditlogResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.auditlog.CreateAuditlogCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.consent.ConsentResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.consent.ConsentStatementResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.consent.GrantConsentCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.educationline.EducationLineResponse;
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
class RegistrationApprenticesSagaApplicationServiceTest {

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

        // Shared test fixtures for apprentice registration
        private UUID personId;
        private UUID loginId;
        private UUID apprenticeId;
        private UUID auditlogId;
        private UUID organizationId;
        private UUID educationLineId;
        private UUID consentStatementId;
        private List<CreatePhoneNumberCmd> phoneCmd;
        private CreateApprenticeRegistrationCmd apprenticeCmd;

        private ResponseLoginCreated loginCreatedResponse;
        private ResponseCompensated compensatedOk;

        @BeforeEach
        void setUp() {
                service = new RegistrationSagaApplicationService(apprentices, auditlogs, educationLines, consents,
                                instructors,
                                logins, organizations, persons);

                // Apprentice registration fixtures
                personId = UUID.randomUUID();
                loginId = UUID.randomUUID();
                apprenticeId = UUID.randomUUID();
                auditlogId = UUID.randomUUID();
                organizationId = UUID.randomUUID();
                educationLineId = UUID.randomUUID();
                consentStatementId = UUID.randomUUID();
                loginCreatedResponse = new ResponseLoginCreated(loginId, "password", LocalDateTime.now());
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

                compensatedOk = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        }

        /*
         * =========================================================== *
         * APPRENTICE REGISTRATION TESTS
         * ===========================================================
         */

        @Nested
        class ApprenticeRegistration {

                @Nested
                class HappyPath {

                        @Test
                        void registerApprentice_shouldReturnCreatedApprenticeResponse_whenAllStepsSucceed() {

                                when(organizations.getById(organizationId))
                                                .thenReturn(mock(OrganisationResponse.class));
                                when(educationLines.getById(educationLineId))
                                                .thenReturn(mock(EducationLineResponse.class));
                                when(consents.getConsentStatementById(consentStatementId))
                                                .thenReturn(mock(ConsentStatementResponse.class));

                                when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);
                                when(persons.getById(personId)).thenReturn(mock(PersonResponse.class));
                                when(logins.create(any(CreateLoginCmd.class))).thenReturn(loginCreatedResponse);
                                when(logins.getById(loginId)).thenReturn(mock(LoginResponse.class));
                                when(apprentices.create(any(CreateApprenticeCmd.class))).thenReturn(apprenticeId);
                                when(apprentices.getById(apprenticeId)).thenReturn(mock(ApprenticeResponse.class));
                                when(consents.grant(any(GrantConsentCmd.class))).thenReturn(consentStatementId);
                                when(consents.getById(consentStatementId)).thenReturn(mock(ConsentResponse.class));
                                when(auditlogs.create(any(CreateAuditlogCmd.class))).thenReturn(auditlogId);
                                when(auditlogs.getById(auditlogId)).thenReturn(mock(AuditlogResponse.class));

                                // Execute
                                CreatedApprenticeResponse result = service.registerApprentice(apprenticeCmd);

                                // Verify
                                assertNotNull(result);
                                assertEquals(apprenticeId, result.apprenticeId());
                        }

                        @Test
                        void registerApprentice_shouldInvokeAllStepsInCorrectOrder_whenAllStepsSucceed() {
                                // Setup - Verify dependencies exist
                                when(organizations.getById(apprenticeCmd.organizationRef()))
                                                .thenReturn(mock(OrganisationResponse.class));
                                when(educationLines.getById(apprenticeCmd.educationLineRef()))
                                                .thenReturn(mock(EducationLineResponse.class));
                                when(consents.getConsentStatementById(apprenticeCmd.consentStatementId()))
                                                .thenReturn(mock(ConsentStatementResponse.class));

                                when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);
                                when(persons.getById((personId))).thenReturn(mock(PersonResponse.class));
                                when(logins.create(any(CreateLoginCmd.class))).thenReturn(loginCreatedResponse);
                                when(logins.getById(loginId)).thenReturn(mock(LoginResponse.class));
                                when(apprentices.create(any(CreateApprenticeCmd.class))).thenReturn(apprenticeId);
                                when(apprentices.getById((apprenticeId))).thenReturn(mock(ApprenticeResponse.class));
                                when(consents.grant(any(GrantConsentCmd.class))).thenReturn(consentStatementId);
                                when(consents.getById(consentStatementId)).thenReturn(mock(ConsentResponse.class));
                                when(auditlogs.create(any(CreateAuditlogCmd.class))).thenReturn(auditlogId);
                                when(auditlogs.getById((auditlogId))).thenReturn(mock(AuditlogResponse.class));

                                // Execute
                                service.registerApprentice(apprenticeCmd);

                                // Verify order
                                var inOrder = inOrder(organizations, educationLines, consents, persons, logins,
                                                apprentices, auditlogs);
                                inOrder.verify(organizations).getById(apprenticeCmd.organizationRef());
                                inOrder.verify(educationLines).getById(apprenticeCmd.educationLineRef());
                                inOrder.verify(consents).getConsentStatementById(apprenticeCmd.consentStatementId());
                                inOrder.verify(persons).create(any(CreatePersonCmd.class));
                                inOrder.verify(persons).getById(personId);
                                inOrder.verify(logins).create(any(CreateLoginCmd.class));
                                inOrder.verify(logins).getById((loginId));
                                inOrder.verify(apprentices).create(any(CreateApprenticeCmd.class));
                                inOrder.verify(apprentices).getById((apprenticeId));
                                inOrder.verify(consents).grant(any(GrantConsentCmd.class));
                                inOrder.verify(consents).getById((consentStatementId));
                                inOrder.verify(auditlogs).create(any(CreateAuditlogCmd.class));
                                inOrder.verify(auditlogs).getById(auditlogId);
                        }

                        @Test
                        void registerApprentice_shouldNeverCallCompensate_whenAllStepsSucceed() {
                                // Setup - Verify dependencies exist
                                when(organizations.getById(apprenticeCmd.organizationRef()))
                                                .thenReturn(mock(OrganisationResponse.class));
                                when(educationLines.getById(apprenticeCmd.educationLineRef()))
                                                .thenReturn(mock(EducationLineResponse.class));
                                when(consents.getConsentStatementById(apprenticeCmd.consentStatementId()))
                                                .thenReturn(mock(ConsentStatementResponse.class));

                                when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);
                                when(persons.getById((personId))).thenReturn(mock(PersonResponse.class));
                                when(logins.create(any(CreateLoginCmd.class))).thenReturn(loginCreatedResponse);
                                when(logins.getById((loginId))).thenReturn(mock(LoginResponse.class));
                                when(apprentices.create(any(CreateApprenticeCmd.class))).thenReturn(apprenticeId);
                                when(apprentices.getById((apprenticeId))).thenReturn(mock(ApprenticeResponse.class));
                                when(consents.grant(any(GrantConsentCmd.class))).thenReturn(consentStatementId);
                                when(consents.getById(consentStatementId)).thenReturn(mock(ConsentResponse.class));
                                when(auditlogs.create(any(CreateAuditlogCmd.class))).thenReturn(auditlogId);
                                when(auditlogs.getById((auditlogId))).thenReturn(mock(AuditlogResponse.class));

                                // Execute
                                service.registerApprentice(apprenticeCmd);

                                // Verify no compensations
                                verify(persons, never()).compensate(any(), any(), any());
                                verify(logins, never()).compensate(any(), any(), any());
                                verify(apprentices, never()).compensate(any(), any(), any());
                                verify(consents, never()).compensate(any(), any(), any());
                                verify(auditlogs, never()).compensate(any(), any(), any());
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
                        }

                        @Nested
                        class PersonFailCases {

                                @Test
                                void registerApprentice_shouldThrowRuntimeException_whenPersonCreateReturnsNull() {
                                        when(organizations.getById(apprenticeCmd.organizationRef()))
                                                        .thenReturn(mock(OrganisationResponse.class));
                                        when(educationLines.getById(apprenticeCmd.educationLineRef()))
                                                        .thenReturn(mock(EducationLineResponse.class));
                                        when(consents.getConsentStatementById(apprenticeCmd.consentStatementId()))
                                                        .thenReturn(mock(ConsentStatementResponse.class));
                                        when(persons.create(any(CreatePersonCmd.class))).thenReturn(null);

                                        assertThrows(Exception.class, () -> service.registerApprentice(apprenticeCmd));
                                }


                                @Test
                                void registerApprentice_shouldThrowRuntimeException_whenPersonCreateThrowsException() {
                                        when(organizations.getById(apprenticeCmd.organizationRef()))
                                                        .thenReturn(mock(OrganisationResponse.class));
                                        when(educationLines.getById(apprenticeCmd.educationLineRef()))
                                                        .thenReturn(mock(EducationLineResponse.class));
                                        when(consents.getConsentStatementById(apprenticeCmd.consentStatementId()))
                                                        .thenReturn(mock(ConsentStatementResponse.class));
                                        when(persons.create(any(CreatePersonCmd.class)))
                                                        .thenThrow(new RuntimeException("person service down"));

                                        assertThrows(Exception.class, () -> service.registerApprentice(apprenticeCmd));
                                }

                                @Test
                                void registerApprentice_shouldNotCompensate_whenPersonCreateFails() {
                                        when(organizations.getById(apprenticeCmd.organizationRef()))
                                                        .thenReturn(mock(OrganisationResponse.class));
                                        when(educationLines.getById(apprenticeCmd.educationLineRef()))
                                                        .thenReturn(mock(EducationLineResponse.class));
                                        when(consents.getConsentStatementById(apprenticeCmd.consentStatementId()))
                                                        .thenReturn(mock(ConsentStatementResponse.class));
                                        when(persons.create(any(CreatePersonCmd.class)))
                                                        .thenThrow(new RuntimeException("person service down"));

                                        assertThrows(Exception.class, () -> service.registerApprentice(apprenticeCmd));

                                        verify(persons, never()).compensate(any(), any(), any());
                                        verify(logins, never()).compensate(any(), any(), any());
                                        verify(apprentices, never()).compensate(any(), any(), any());
                                        verify(auditlogs, never()).compensate(any(), any(), any());
                                }

                                @Test
                                void registerApprentice_shouldThrowConflictException_whenPersonCreationFails() {
                                        when(organizations.getById(apprenticeCmd.organizationRef()))
                                                        .thenReturn(mock(OrganisationResponse.class));
                                        when(educationLines.getById(apprenticeCmd.educationLineRef()))
                                                        .thenReturn(mock(EducationLineResponse.class));
                                        when(consents.getConsentStatementById(apprenticeCmd.consentStatementId()))
                                                        .thenReturn(mock(ConsentStatementResponse.class));

                                        when(persons.create(any(CreatePersonCmd.class)))
                                                        .thenThrow(new RuntimeException("person service down"));

                                        // Act + Assert
                                        ConflictException ex = assertThrows(ConflictException.class,
                                                        () -> service.registerApprentice(apprenticeCmd));

                                        assertEquals("person.not.created", ex.messageKey());
                                }

                                @Test
                                void registerApprentice_shouldThrowConflictException_whenPersonRetrievalFails() {
                                        when(organizations.getById(apprenticeCmd.organizationRef()))
                                                        .thenReturn(mock(OrganisationResponse.class));
                                        when(educationLines.getById(apprenticeCmd.educationLineRef()))
                                                        .thenReturn(mock(EducationLineResponse.class));
                                        when(consents.getConsentStatementById(apprenticeCmd.consentStatementId()))
                                                        .thenReturn(mock(ConsentStatementResponse.class));

                                        when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);

                                        when(persons.getById(personId))
                                                        .thenThrow(new RuntimeException("person service down"));

                                        when(persons.compensate(any(), any(), any()))
                                                        .thenReturn(compensatedOk);

                                        ConflictException ex = assertThrows(ConflictException.class,
                                                        () -> service.registerApprentice(apprenticeCmd));

                                        assertEquals("person.read.failed", ex.messageKey());
                                }

                                @Test
                                void registerAppdrentice_shouldThrowConflictException_whenPersonDoesNotExist() {
                                        when(organizations.getById((apprenticeCmd.organizationRef())))
                                                        .thenReturn(mock(OrganisationResponse.class));

                                        when(educationLines.getById((apprenticeCmd.educationLineRef())))
                                                        .thenReturn(mock(EducationLineResponse.class));

                                        when(consents.getConsentStatementById((apprenticeCmd.consentStatementId())))
                                                        .thenReturn(mock(ConsentStatementResponse.class));

                                        when(persons.create(any())).thenReturn(personId);

                                        when(persons.getById(personId)).thenReturn(null);

                                        // compensate response
                                        when(persons.compensate(any(), any(), any()))
                                                        .thenReturn(compensatedOk);

                                        // Act + Assert
                                        ConflictException ex = assertThrows(
                                                        ConflictException.class,
                                                        () -> service.registerApprentice(apprenticeCmd));

                                        assertEquals("person.not.found", ex.getMessage());

                                        // Verify compensation happened
                                        verify(persons).compensate(any(), any(), any());
                                }
                        }

                        @Nested
                        class LoginFailCases {
                                @Test
                                void registerApprentice_shouldThrowConflictException_whenLoginCreationFails() {
                                        when(organizations.getById(apprenticeCmd.organizationRef()))
                                                        .thenReturn(mock(OrganisationResponse.class));
                                        when(educationLines.getById(apprenticeCmd.educationLineRef()))
                                                        .thenReturn(mock(EducationLineResponse.class));
                                        when(consents.getConsentStatementById(apprenticeCmd.consentStatementId()))
                                                        .thenReturn(mock(ConsentStatementResponse.class));

                                        when(persons.create(any(CreatePersonCmd.class)))
                                                        .thenReturn(personId);

                                        when(persons.getById(personId))
                                                        .thenReturn(mock(PersonResponse.class));

                                        when(logins.create(any(CreateLoginCmd.class)))
                                                        .thenThrow(new RuntimeException("login service down"));

                                        when(persons.compensate(eq(personId), any(), any()))
                                                        .thenReturn(compensatedOk);

                                        ConflictException ex = assertThrows(ConflictException.class,
                                                        () -> service.registerApprentice(apprenticeCmd));

                                        assertEquals("login.not.created", ex.messageKey());
                                }

                                @Test
                                void registerApprentice_shouldThrowConflictException_whenLoginRetrievalFails() {
                                        when(organizations.getById(apprenticeCmd.organizationRef()))
                                                        .thenReturn(mock(OrganisationResponse.class));
                                        when(educationLines.getById(apprenticeCmd.educationLineRef()))
                                                        .thenReturn(mock(EducationLineResponse.class));
                                        when(consents.getConsentStatementById(apprenticeCmd.consentStatementId()))
                                                        .thenReturn(mock(ConsentStatementResponse.class));

                                        when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);

                                        when(persons.getById(personId))
                                                        .thenReturn(mock(PersonResponse.class));

                                        when(logins.create(any(CreateLoginCmd.class)))
                                                        .thenReturn(loginCreatedResponse);

                                        when(logins.getById(loginId))
                                                        .thenThrow(new RuntimeException("login service down"));

                                        when(logins.compensate(eq(loginId), any(), any()))
                                                        .thenReturn(compensatedOk);
                                        when(persons.compensate(eq(personId), any(), any()))
                                                        .thenReturn(compensatedOk);

                                        ConflictException ex = assertThrows(ConflictException.class,
                                                        () -> service.registerApprentice(apprenticeCmd));

                                        assertEquals("login.read.failed", ex.messageKey());
                                }

                                @Test
                                void registerAppdrentice_shouldThrowConflictException_whenLoginDoesNotExist() {
                                        when(organizations.getById((apprenticeCmd.organizationRef())))
                                                        .thenReturn(mock(OrganisationResponse.class));

                                        when(educationLines.getById((apprenticeCmd.educationLineRef())))
                                                        .thenReturn(mock(EducationLineResponse.class));

                                        when(consents.getConsentStatementById((apprenticeCmd.consentStatementId())))
                                                        .thenReturn(mock(ConsentStatementResponse.class));

                                        when(persons.create(any())).thenReturn(personId);

                                        when(persons.getById(personId)).thenReturn(mock(PersonResponse.class));

                                        when(logins.create(any(CreateLoginCmd.class)))
                                                        .thenReturn(loginCreatedResponse);

                                        when(logins.getById(loginId))
                                                        .thenReturn(null);

                                        when(logins.compensate(eq(loginId), any(), any())).thenReturn(compensatedOk);
                                        when(persons.compensate(eq(personId), any(), any())).thenReturn(compensatedOk);

                                        ConflictException ex = assertThrows(
                                                        ConflictException.class,
                                                        () -> service.registerApprentice(apprenticeCmd));

                                        assertEquals("login.not.found", ex.getMessage());

                                }
                        }

                        @Nested
                        class ApprenticeFailCases {
                                @Test
                                void registerApprentice_shouldThrowConflictException_whenApprenticeCreationFails() {
                                        when(organizations.getById(apprenticeCmd.organizationRef()))
                                                        .thenReturn(mock(OrganisationResponse.class));
                                        when(educationLines.getById(apprenticeCmd.educationLineRef()))
                                                        .thenReturn(mock(EducationLineResponse.class));
                                        when(consents.getConsentStatementById(apprenticeCmd.consentStatementId()))
                                                        .thenReturn(mock(ConsentStatementResponse.class));

                                        when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);

                                        when(persons.getById(personId)).thenReturn(mock(PersonResponse.class));

                                        when(logins.create(any(CreateLoginCmd.class)))
                                                        .thenReturn(loginCreatedResponse);

                                        when(logins.getById(loginId)).thenReturn(mock(LoginResponse.class));

                                        when(apprentices.create(any(CreateApprenticeCmd.class))).thenThrow(
                                                        new RuntimeException("apprentice service unavailable"));

                                        when(logins.compensate(eq(loginId), any(), any())).thenReturn(compensatedOk);
                                        when(persons.compensate(eq(personId), any(), any())).thenReturn(compensatedOk);

                                        ConflictException ex = assertThrows(ConflictException.class,
                                                        () -> service.registerApprentice(apprenticeCmd));

                                        assertEquals("apprentice.not.created", ex.messageKey());
                                }

                                @Test
                                void registerApprentice_shouldThrowConflictException_whenApprenticeRetrievalFails() {
                                        when(organizations.getById(apprenticeCmd.organizationRef()))
                                                        .thenReturn(mock(OrganisationResponse.class));
                                        when(educationLines.getById(apprenticeCmd.educationLineRef()))
                                                        .thenReturn(mock(EducationLineResponse.class));
                                        when(consents.getConsentStatementById(apprenticeCmd.consentStatementId()))
                                                        .thenReturn(mock(ConsentStatementResponse.class));

                                        when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);

                                        when(persons.getById(personId)).thenReturn(mock(PersonResponse.class));

                                        when(logins.create(any(CreateLoginCmd.class)))
                                                        .thenReturn(loginCreatedResponse);

                                        when(logins.getById(loginId)).thenReturn(mock(LoginResponse.class));

                                        when(apprentices.create(any(CreateApprenticeCmd.class)))
                                                        .thenReturn(apprenticeId);

                                        when(apprentices.getById(apprenticeId)).thenThrow(
                                                        new RuntimeException("apprentice service unavailable"));

                                        when(apprentices.compensate(eq(apprenticeId), any(), any()))
                                                        .thenReturn(compensatedOk);
                                        when(logins.compensate(eq(loginId), any(), any())).thenReturn(compensatedOk);
                                        when(persons.compensate(eq(personId), any(), any())).thenReturn(compensatedOk);

                                        ConflictException ex = assertThrows(ConflictException.class,
                                                        () -> service.registerApprentice(apprenticeCmd));

                                        assertEquals("apprentice.read.failed", ex.messageKey());
                                }

                                @Test
                                void registerAppdrentice_shouldThrowConflictException_whenApprenticeDoesNotExist() {
                                        when(organizations.getById(apprenticeCmd.organizationRef()))
                                                        .thenReturn(mock(OrganisationResponse.class));
                                        when(educationLines.getById(apprenticeCmd.educationLineRef()))
                                                        .thenReturn(mock(EducationLineResponse.class));
                                        when(consents.getConsentStatementById(apprenticeCmd.consentStatementId()))
                                                        .thenReturn(mock(ConsentStatementResponse.class));

                                        when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);

                                        when(persons.getById(personId)).thenReturn(mock(PersonResponse.class));

                                        when(logins.create(any(CreateLoginCmd.class)))
                                                        .thenReturn(loginCreatedResponse);

                                        when(logins.getById(loginId)).thenReturn(mock(LoginResponse.class));

                                        when(apprentices.create(any(CreateApprenticeCmd.class)))
                                                        .thenReturn(apprenticeId);

                                        when(apprentices.getById(apprenticeId)).thenReturn(null);

                                        when(apprentices.compensate(eq(apprenticeId), any(), any()))
                                                        .thenReturn(compensatedOk);
                                        when(logins.compensate(eq(loginId), any(), any())).thenReturn(compensatedOk);
                                        when(persons.compensate(eq(personId), any(), any())).thenReturn(compensatedOk);

                                        ConflictException ex = assertThrows(ConflictException.class,
                                                        () -> service.registerApprentice(apprenticeCmd));

                                        assertEquals("apprentice.not.found", ex.messageKey());

                                }
                        }

                        @Nested
                        class ConsentFailCases {
                                @Test
                                void registerApprentice_shouldThrowConflictException_whenConsentCreationFails() {
                                        when(organizations.getById(apprenticeCmd.organizationRef()))
                                                        .thenReturn(mock(OrganisationResponse.class));
                                        when(educationLines.getById(apprenticeCmd.educationLineRef()))
                                                        .thenReturn(mock(EducationLineResponse.class));
                                        when(consents.getConsentStatementById(apprenticeCmd.consentStatementId()))
                                                        .thenReturn(mock(ConsentStatementResponse.class));

                                        when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);

                                        when(persons.getById(personId)).thenReturn(mock(PersonResponse.class));

                                        when(logins.create(any(CreateLoginCmd.class)))
                                                        .thenReturn(loginCreatedResponse);

                                        when(logins.getById(loginId)).thenReturn(mock(LoginResponse.class));

                                        when(apprentices.create(any(CreateApprenticeCmd.class)))
                                                        .thenReturn(apprenticeId);
                                        when(apprentices.getById(apprenticeId))
                                                        .thenReturn(mock(ApprenticeResponse.class));
                                        when(consents.grant(any(GrantConsentCmd.class)))
                                                        .thenThrow(new RuntimeException("consent service unavailable"));

                                        when(apprentices.compensate(eq(apprenticeId), any(), any()))
                                                        .thenReturn(compensatedOk);
                                        when(logins.compensate(eq(loginId), any(), any())).thenReturn(compensatedOk);
                                        when(persons.compensate(eq(personId), any(), any())).thenReturn(compensatedOk);

                                        ConflictException ex = assertThrows(ConflictException.class,
                                                        () -> service.registerApprentice(apprenticeCmd));

                                        assertEquals("consent.not.granted", ex.messageKey());
                                }

                                @Test
                                void registerApprentice_shouldThrowConflictException_whenConsentRetrievalFails() {
                                        when(organizations.getById(apprenticeCmd.organizationRef()))
                                                        .thenReturn(mock(OrganisationResponse.class));
                                        when(educationLines.getById(apprenticeCmd.educationLineRef()))
                                                        .thenReturn(mock(EducationLineResponse.class));
                                        when(consents.getConsentStatementById(apprenticeCmd.consentStatementId()))
                                                        .thenReturn(mock(ConsentStatementResponse.class));

                                        when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);

                                        when(persons.getById(personId)).thenReturn(mock(PersonResponse.class));

                                        when(logins.create(any(CreateLoginCmd.class)))
                                                        .thenReturn(loginCreatedResponse);

                                        when(logins.getById(loginId)).thenReturn(mock(LoginResponse.class));

                                        when(apprentices.create(any(CreateApprenticeCmd.class)))
                                                        .thenReturn(apprenticeId);

                                        when(apprentices.getById(apprenticeId))
                                                        .thenReturn(mock(ApprenticeResponse.class));
                                        when(consents.grant(any(GrantConsentCmd.class)))
                                                        .thenReturn(consentStatementId);
                                        when(consents.getById(consentStatementId))
                                                        .thenThrow(new RuntimeException("consent service unavailable"));

                                        when(consents.compensate(any(), any(), any())).thenReturn(compensatedOk);
                                        when(apprentices.compensate(eq(apprenticeId), any(), any()))
                                                        .thenReturn(compensatedOk);
                                        when(logins.compensate(eq(loginId), any(), any())).thenReturn(compensatedOk);
                                        when(persons.compensate(eq(personId), any(), any())).thenReturn(compensatedOk);

                                        ConflictException ex = assertThrows(ConflictException.class,
                                                        () -> service.registerApprentice(apprenticeCmd));

                                        assertEquals("consent.read.failed", ex.messageKey());
                                }

                                @Test
                                void registerAppdrentice_shouldThrowConflictException_whenConsentDoesNotExist() {
                                        when(organizations.getById(apprenticeCmd.organizationRef()))
                                                        .thenReturn(mock(OrganisationResponse.class));
                                        when(educationLines.getById(apprenticeCmd.educationLineRef()))
                                                        .thenReturn(mock(EducationLineResponse.class));
                                        when(consents.getConsentStatementById(apprenticeCmd.consentStatementId()))
                                                        .thenReturn(mock(ConsentStatementResponse.class));

                                        when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);

                                        when(persons.getById(personId)).thenReturn(mock(PersonResponse.class));

                                        when(logins.create(any(CreateLoginCmd.class)))
                                                        .thenReturn(loginCreatedResponse);

                                        when(logins.getById(loginId)).thenReturn(mock(LoginResponse.class));

                                        when(apprentices.create(any(CreateApprenticeCmd.class)))
                                                        .thenReturn(apprenticeId);

                                        when(apprentices.getById(apprenticeId))
                                                        .thenReturn(mock(ApprenticeResponse.class));
                                        when(consents.grant(any(GrantConsentCmd.class)))
                                                        .thenReturn(consentStatementId);
                                        when(consents.getById(consentStatementId)).thenReturn(null);

                                        when(consents.compensate(any(), any(), any())).thenReturn(compensatedOk);
                                        when(apprentices.compensate(eq(apprenticeId), any(), any()))
                                                        .thenReturn(compensatedOk);
                                        when(logins.compensate(eq(loginId), any(), any())).thenReturn(compensatedOk);
                                        when(persons.compensate(eq(personId), any(), any())).thenReturn(compensatedOk);

                                        ConflictException ex = assertThrows(ConflictException.class,
                                                        () -> service.registerApprentice(apprenticeCmd));

                                        assertEquals("consent.not.found", ex.messageKey());

                                }
                        }

                        @Nested
                        class AuditLogFailCases {
                                @Test
                                void registerApprentice_shouldThrowConflictException_whenAuditLogCreationFails() {
                                        when(organizations.getById(apprenticeCmd.organizationRef()))
                                                        .thenReturn(mock(OrganisationResponse.class));
                                        when(educationLines.getById(apprenticeCmd.educationLineRef()))
                                                        .thenReturn(mock(EducationLineResponse.class));
                                        when(consents.getConsentStatementById(apprenticeCmd.consentStatementId()))
                                                        .thenReturn(mock(ConsentStatementResponse.class));

                                        when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);
                                        when(persons.getById(personId)).thenReturn(mock(PersonResponse.class));
                                        when(logins.create(any(CreateLoginCmd.class)))
                                                        .thenReturn(loginCreatedResponse);
                                        when(logins.getById(loginId)).thenReturn(mock(LoginResponse.class));
                                        when(apprentices.create(any(CreateApprenticeCmd.class)))
                                                        .thenReturn(apprenticeId);
                                        when(apprentices.getById(apprenticeId))
                                                        .thenReturn(mock(ApprenticeResponse.class));
                                        when(consents.grant(any(GrantConsentCmd.class)))
                                                        .thenReturn(consentStatementId);
                                        when(consents.getById(consentStatementId))
                                                        .thenReturn(mock(ConsentResponse.class));

                                        when(auditlogs.create(any(CreateAuditlogCmd.class))).thenThrow(
                                                        new RuntimeException("auditlog service unavailable"));

                                        when(consents.compensate(any(), any(), any())).thenReturn(compensatedOk);
                                        when(apprentices.compensate(eq(apprenticeId), any(), any()))
                                                        .thenReturn(compensatedOk);
                                        when(logins.compensate(eq(loginId), any(), any())).thenReturn(compensatedOk);
                                        when(persons.compensate(eq(personId), any(), any())).thenReturn(compensatedOk);

                                        ConflictException ex = assertThrows(ConflictException.class,
                                                        () -> service.registerApprentice(apprenticeCmd));

                                        assertEquals("auditlog.not.created", ex.messageKey());
                                }

                                @Test
                                void registerApprentice_shouldThrowConflictException_whenAuditLogRetrievalFails() {
                                        when(organizations.getById(apprenticeCmd.organizationRef()))
                                                        .thenReturn(mock(OrganisationResponse.class));
                                        when(educationLines.getById(apprenticeCmd.educationLineRef()))
                                                        .thenReturn(mock(EducationLineResponse.class));
                                        when(consents.getConsentStatementById(apprenticeCmd.consentStatementId()))
                                                        .thenReturn(mock(ConsentStatementResponse.class));

                                        when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);
                                        when(persons.getById(personId)).thenReturn(mock(PersonResponse.class));
                                        when(logins.create(any(CreateLoginCmd.class)))
                                                        .thenReturn(loginCreatedResponse);
                                        when(logins.getById(loginId)).thenReturn(mock(LoginResponse.class));
                                        when(apprentices.create(any(CreateApprenticeCmd.class)))
                                                        .thenReturn(apprenticeId);
                                        when(apprentices.getById(apprenticeId))
                                                        .thenReturn(mock(ApprenticeResponse.class));
                                        when(consents.grant(any(GrantConsentCmd.class)))
                                                        .thenReturn(consentStatementId);
                                        when(consents.getById(consentStatementId))
                                                        .thenReturn(mock(ConsentResponse.class));
                                        when(auditlogs.create(any(CreateAuditlogCmd.class))).thenReturn(auditlogId);

                                        when(auditlogs.getById(auditlogId)).thenThrow(
                                                        new RuntimeException("auditlog service unavailable"));

                                        when(auditlogs.compensate(any(), any(), any())).thenReturn(compensatedOk);
                                        when(consents.compensate(any(), any(), any())).thenReturn(compensatedOk);
                                        when(apprentices.compensate(eq(apprenticeId), any(), any()))
                                                        .thenReturn(compensatedOk);
                                        when(logins.compensate(eq(loginId), any(), any())).thenReturn(compensatedOk);
                                        when(persons.compensate(eq(personId), any(), any())).thenReturn(compensatedOk);

                                        ConflictException ex = assertThrows(ConflictException.class,
                                                        () -> service.registerApprentice(apprenticeCmd));

                                        assertEquals("auditlog.read.failed", ex.messageKey());
                                }

                                @Test
                                void registerAppdrentice_shouldThrowConflictException_whenAuditLogDoesNotExist() {
                                        when(organizations.getById(apprenticeCmd.organizationRef()))
                                                        .thenReturn(mock(OrganisationResponse.class));
                                        when(educationLines.getById(apprenticeCmd.educationLineRef()))
                                                        .thenReturn(mock(EducationLineResponse.class));
                                        when(consents.getConsentStatementById(apprenticeCmd.consentStatementId()))
                                                        .thenReturn(mock(ConsentStatementResponse.class));

                                        when(persons.create(any(CreatePersonCmd.class))).thenReturn(personId);
                                        when(persons.getById(personId)).thenReturn(mock(PersonResponse.class));
                                        when(logins.create(any(CreateLoginCmd.class)))
                                                        .thenReturn(loginCreatedResponse);
                                        when(logins.getById(loginId)).thenReturn(mock(LoginResponse.class));
                                        when(apprentices.create(any(CreateApprenticeCmd.class)))
                                                        .thenReturn(apprenticeId);
                                        when(apprentices.getById(apprenticeId))
                                                        .thenReturn(mock(ApprenticeResponse.class));
                                        when(consents.grant(any(GrantConsentCmd.class)))
                                                        .thenReturn(consentStatementId);
                                        when(consents.getById(consentStatementId))
                                                        .thenReturn(mock(ConsentResponse.class));
                                        when(auditlogs.create(any(CreateAuditlogCmd.class))).thenReturn(auditlogId);

                                        when(auditlogs.getById(auditlogId)).thenReturn(null);

                                        when(auditlogs.compensate(any(), any(), any())).thenReturn(compensatedOk);
                                        when(consents.compensate(any(), any(), any())).thenReturn(compensatedOk);
                                        when(apprentices.compensate(eq(apprenticeId), any(), any()))
                                                        .thenReturn(compensatedOk);
                                        when(logins.compensate(eq(loginId), any(), any())).thenReturn(compensatedOk);
                                        when(persons.compensate(eq(personId), any(), any())).thenReturn(compensatedOk);

                                        ConflictException ex = assertThrows(ConflictException.class,
                                                        () -> service.registerApprentice(apprenticeCmd));

                                        assertEquals("auditlog.not.found", ex.messageKey());

                                }
                        }
                }
        }
}