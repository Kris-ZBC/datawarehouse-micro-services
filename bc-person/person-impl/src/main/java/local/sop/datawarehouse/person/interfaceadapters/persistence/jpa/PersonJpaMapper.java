package local.sop.datawarehouse.person.interfaceadapters.persistence.jpa;

import java.util.List;

import org.springframework.stereotype.Component;

import local.sop.datawarehouse.person.domain.model.Person;
import local.sop.datawarehouse.person.domain.model.PhoneNumber;
import local.sop.datawarehouse.person.domain.model.valueobjects.Email;
import local.sop.datawarehouse.person.domain.model.valueobjects.FirstName;
import local.sop.datawarehouse.person.domain.model.valueobjects.LastName;
import local.sop.datawarehouse.person.domain.model.valueobjects.OrganizationRef;
import local.sop.datawarehouse.person.domain.model.valueobjects.PersonId;
import local.sop.datawarehouse.person.domain.model.valueobjects.PhoneNumberId;
import local.sop.datawarehouse.person.domain.model.valueobjects.PhoneNumberValue;

@Component
public class PersonJpaMapper {

	public Person toDomain(PersonEntity entity) {
		Person.Builder builder = Person.builder()
				.id(new PersonId(entity.getId()))
				.firstName(new FirstName(entity.getFirstName()))
				.lastName(new LastName(entity.getLastName()))
				.email(new Email(entity.getEmail()))
				.organizationRef(new OrganizationRef(entity.getOrganizationRef()));

		if (entity.getPhoneNumbers() != null) {
			for (PhoneNumberEntity phoneNumberEntity : entity.getPhoneNumbers()) {
				builder.phoneNumber(new PhoneNumber(
						new PhoneNumberId(phoneNumberEntity.getId()),
						phoneNumberEntity.getType(),
						new PhoneNumberValue(phoneNumberEntity.getPhoneNumber())));
			}
		}

		return builder.build();
	}

	public PersonEntity toEntity(Person domain) {
		PersonEntity entity = new PersonEntity();
		copyIntoEntity(domain, entity);
		return entity;
	}
	
	public void copyIntoEntity(Person domain, PersonEntity entity) {
		entity.setId(domain.getId().value());
		entity.setFirstName(domain.getFirstName().value());
		entity.setLastName(domain.getLastName().value());
		entity.setEmail(domain.getEmail().value());
		entity.setOrganizationRef(domain.getOrganizationRef().value());

		List<PhoneNumberEntity> phoneNumberEntities = domain.getPhoneNumbers().stream()
				.map(phoneNumber -> new PhoneNumberEntity(
						phoneNumber.getId().value(),
						phoneNumber.getType(),
						phoneNumber.getValue().value()))
				.toList();

		entity.replacePhoneNumbers(phoneNumberEntities);
	}
}