package local.sop.datawarehouse.person.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.datawarehouse.person.domain.model.Person;
import local.sop.datawarehouse.person.domain.model.valueobjects.Email;
import local.sop.datawarehouse.person.domain.model.valueobjects.PersonId;
import local.sop.datawarehouse.person.domain.ports.out.PersonRepositoryPort;

@Repository
public class PersonRepositoryJpaAdapter implements PersonRepositoryPort{
	private final PersonSpringDataRepository personSpringDataRepository;
	private final PersonJpaMapper personJpaMapper;

	public PersonRepositoryJpaAdapter(
			PersonSpringDataRepository personSpringDataRepository,
			PersonJpaMapper personJpaMapper
	) {
		this.personSpringDataRepository = personSpringDataRepository;
		this.personJpaMapper = personJpaMapper;
	}
	
	@Override
	public Person save(Person person) {
		PersonEntity entity = personSpringDataRepository.findById(person.getId().value())
				.map(existingEntity -> {
					personJpaMapper.copyIntoEntity(person, existingEntity);
					return existingEntity;
				})
				.orElseGet(() -> personJpaMapper.toEntity(person));

		PersonEntity savedEntity = personSpringDataRepository.save(entity);
		return personJpaMapper.toDomain(savedEntity);
	}

	@Override
	public List<Person> findAll() {
		return personSpringDataRepository.findAll()
				.stream()
				.map(personJpaMapper::toDomain)
				.toList();
	}

	@Override
	public Optional<Person> findById(PersonId id) {
		return personSpringDataRepository.findById(id.value())
				.map(personJpaMapper::toDomain);
	}

	@Override
	public List<Person> searchByName(String name) {
		return personSpringDataRepository
				.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name)
				.stream()
				.map(personJpaMapper::toDomain)
				.toList();
	}
	
	@Override
	public Optional<Person> findByEmail(Email email) {
		return personSpringDataRepository.findByEmail(email.value())
				.map(personJpaMapper::toDomain);
	}

	 @Override
    public Boolean compensate(PersonId id, SagaOutcome sagaState) {
        if(sagaState != SagaOutcome.COMPENSATE)
            throw new ConflictException("compensate.wrong_state", Map.of("compensate", sagaState.name()));
        var found = findById(id);
        if(found.isEmpty()) {
            return false;
        }
        return (personSpringDataRepository.delete(id.value()) == 1? true: false);
    }

}
