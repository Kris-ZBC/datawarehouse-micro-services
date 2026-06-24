package local.sop.sopinfo.person.domain.ports.out;

import java.util.List;
import java.util.Optional;

import local.sop.sopinfo.person.domain.model.Person;
import local.sop.sopinfo.person.domain.model.valueobjects.Email;
import local.sop.sopinfo.person.domain.model.valueobjects.PersonId;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

public interface PersonRepositoryPort {
    Person save(Person person);
    Optional<Person> findById(PersonId id);
    List<Person> findAll();
    List<Person> searchByName(String name);
	Optional<Person> findByEmail(Email email);
    Boolean compensate(PersonId id, SagaOutcome state);
}