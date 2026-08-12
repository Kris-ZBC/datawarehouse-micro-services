package local.sop.datawarehouse.person.application;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import local.sop.datawarehouse.person.application.api.PersonDirectory;
import local.sop.datawarehouse.person.application.api.dto.AddPhoneNumberCmd;
import local.sop.datawarehouse.person.application.api.dto.CreatePersonCmd;
import local.sop.datawarehouse.person.application.api.dto.PersonResponse;
import local.sop.datawarehouse.person.application.api.dto.PhoneNumberResponse;
import local.sop.datawarehouse.person.application.api.dto.UpdatePersonCmd;
import local.sop.datawarehouse.person.application.mapper.PersonResponseMapper;
import local.sop.datawarehouse.person.domain.model.Person;
import local.sop.datawarehouse.person.domain.model.PhoneNumber;
import local.sop.datawarehouse.person.domain.model.PhoneNumberDraft;
import local.sop.datawarehouse.person.domain.model.valueobjects.Email;
import local.sop.datawarehouse.person.domain.model.valueobjects.FirstName;
import local.sop.datawarehouse.person.domain.model.valueobjects.LastName;
import local.sop.datawarehouse.person.domain.model.valueobjects.OrganizationRef;
import local.sop.datawarehouse.person.domain.model.valueobjects.PersonId;
import local.sop.datawarehouse.person.domain.model.valueobjects.PhoneNumberId;
import local.sop.datawarehouse.person.domain.model.valueobjects.PhoneNumberValue;
import local.sop.datawarehouse.person.domain.ports.out.PersonRepositoryPort;
import local.sop.datawarehouse.person.domain.service.PersonDomain;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;

@Service
public class PersonApplicationService implements PersonDirectory {

	private static final Logger log = LoggerFactory.getLogger(PersonApplicationService.class);

	private final PersonRepositoryPort personRepositoryPort;
	private final PersonDomain personDomain;

	public PersonApplicationService(PersonDomain domain, PersonRepositoryPort personRepositoryPort) {
		this.personDomain = domain;
		this.personRepositoryPort = personRepositoryPort;
	}

	@Override
	@Transactional
	public UUID create(CreatePersonCmd cmd) {
		log.info("Creating person for organizationRef={}", cmd.organizationRef());

		Email email = new Email(cmd.email());

		personRepositoryPort.findByEmail(email).ifPresent(person -> {
			log.warn("Create person rejected because email already exists");
			throw new ConflictException("person.email.exists", Map.of("field", "email"));
		});

		List<PhoneNumberDraft> phoneNumbers = cmd.phoneNumbers() == null
				? List.of()
				: cmd.phoneNumbers().stream()
						.map(phoneNumberCmd -> new PhoneNumberDraft(
								phoneNumberCmd.type(),
								new PhoneNumberValue(phoneNumberCmd.value())))
						.toList();

		Person person = personDomain.create(
				new FirstName(cmd.firstName()),
				new LastName(cmd.lastName()),
				email,
				new OrganizationRef(cmd.organizationRef()),
				phoneNumbers);

		Person savedPerson = personRepositoryPort.save(person);

		log.info("Person created with personId={}", savedPerson.getId().value());
		return savedPerson.getId().value();
	}

	@Override
	@Transactional(readOnly = true)
	public List<PersonResponse> findAll() {
		return personRepositoryPort.findAll()
				.stream()
				.map(PersonResponseMapper::toResponse)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<PersonResponse> findById(UUID id) {
		if (id == null) {
			throw new ValidationException("key.required", Map.of("field", "id"));
		}

		return personRepositoryPort.findById(new PersonId(id))
				.map(PersonResponseMapper::toResponse);
	}

	@Override
	@Transactional(readOnly = true)
	public List<PersonResponse> searchByName(String name) {
		String normalizedName = normalizeSearchTerm(name);

		return personRepositoryPort.searchByName(normalizedName)
				.stream()
				.map(PersonResponseMapper::toResponse)
				.toList();
	}

	@Override
	@Transactional
	public PersonResponse update(UUID id, UpdatePersonCmd cmd) {
		log.info("Updating person with personId={}", id);

		validateUpdateRequest(cmd);

		Person existingPerson = getRequiredPerson(new PersonId(id));
		Person updatedPerson = applyUpdates(existingPerson, cmd);
		Person savedPerson = personRepositoryPort.save(updatedPerson);

		log.info("Person updated successfully for personId={}", id);
		return PersonResponseMapper.toResponse(savedPerson);
	}

	@Override
	@Transactional
	public PhoneNumberResponse addPhoneNumber(UUID id, AddPhoneNumberCmd cmd) {
		log.info("Adding phone number for personId={}", id);

		Person existingPerson = getRequiredPerson(new PersonId(id));

		PhoneNumber phoneNumber = PhoneNumber.builder()
				.type(cmd.type())
				.value(new PhoneNumberValue(cmd.value()))
				.build();

		Person updatedPerson = existingPerson.withAddedPhoneNumber(phoneNumber);
		personRepositoryPort.save(updatedPerson);

		log.info("Phone number added for personId={}", id);
		return PersonResponseMapper.toResponse(phoneNumber);
	}

	@Override
	@Transactional
	public void removePhoneNumber(UUID personId, UUID phoneId) {
		log.info("Removing phone number for personId={}, phoneNumberId={}", personId, phoneId);

		Person existingPerson = getRequiredPerson(new PersonId(personId));
		PhoneNumberId parsedPhoneNumberId = new PhoneNumberId(phoneId);

		if (!existingPerson.hasPhoneNumber(parsedPhoneNumberId)) {
			log.warn("Remove phone number rejected because phone number was not found for personId={}, phoneNumberId={}", personId, phoneId);
			throw new NotFoundException(
					"person.phonenumber.not.found",
					Map.of(
							"field", "phoneNumberId",
							"id", phoneId,
							"personId", personId));
		}

		Person updatedPerson = existingPerson.withRemovedPhoneNumber(parsedPhoneNumberId);
		personRepositoryPort.save(updatedPerson);

		log.info("Phone number removed for personId={}, phoneNumberId={}", personId, phoneId);
	}

	private Person getRequiredPerson(PersonId personId) {
		return personRepositoryPort.findById(personId)
				.orElseThrow(() -> {
					log.warn("Person not found for personId={}", personId.value());
					return new NotFoundException(
							"person.not.found",
							Map.of("field", "personId", "id", personId.value()));
				});
	}

	private Person applyUpdates(Person existingPerson, UpdatePersonCmd cmd) {
		Person updatedPerson = existingPerson;

		// Null means "leave unchanged" for partial update requests.
		if (cmd.firstName() != null) {
			updatedPerson = updatedPerson.withFirstName(new FirstName(cmd.firstName()));
		}

		if (cmd.lastName() != null) {
			updatedPerson = updatedPerson.withLastName(new LastName(cmd.lastName()));
		}

		if (cmd.email() != null) {
			Email updatedEmail = new Email(cmd.email());

			if (!existingPerson.getEmail().equals(updatedEmail)) {
				ensureEmailIsAvailableForUpdate(existingPerson.getId(), updatedEmail);
				updatedPerson = updatedPerson.withEmail(updatedEmail);
			}
		}

		return updatedPerson;
	}

	private void ensureEmailIsAvailableForUpdate(DomainId currentPersonId, Email email) {
		personRepositoryPort.findByEmail(email).ifPresent(person -> {
			if (!person.getId().equals(currentPersonId)) {
				log.warn("Update rejected because email already exists for another person. currentPersonId={}", currentPersonId.value());
				throw new ConflictException("person.email.exists", Map.of("field", "email"));
			}
		});
	}

	private void validateUpdateRequest(UpdatePersonCmd cmd) {
		if (cmd == null
				|| (cmd.firstName() == null
				&& cmd.lastName() == null
				&& cmd.email() == null)) {
			log.warn("Update person rejected because request body did not contain any updatable fields");
			throw new ValidationException("person.update.empty", Map.of());
		}
	}


	private String normalizeSearchTerm(String name) {
		if (name == null) {
			log.warn("Search person rejected because name parameter was null");
			throw new ValidationException("person.search.name.blank", Map.of("field", "name"));
		}

		String trimmedName = name.trim();

		if (trimmedName.isBlank()) {
			log.warn("Search person rejected because name parameter was blank");
			throw new ValidationException("person.search.name.blank", Map.of("field", "name"));
		}

		return trimmedName;
	}

	@Override
	public ResponseCompensated compensate(UUID id, Class<?> clazz, SagaOutcome sagaState) {
        log.info("Compensate called from class {}", clazz.getSimpleName());
        var person = personRepositoryPort.findById(PersonId.of(id));
        if(person.isEmpty()) {
                return new ResponseCompensated(SagaOutcome.IDEMPOTENT, false);
        }
        boolean result = personRepositoryPort.compensate(PersonId.of(id), sagaState);
        if(result) {
                return new ResponseCompensated(SagaOutcome.COMPENSATED, true);
        }
        return new ResponseCompensated(SagaOutcome.IDEMPOTENT, true);
	}

}