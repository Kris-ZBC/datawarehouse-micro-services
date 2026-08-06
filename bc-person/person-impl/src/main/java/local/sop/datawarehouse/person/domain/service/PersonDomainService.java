package local.sop.datawarehouse.person.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;

import local.sop.datawarehouse.person.domain.model.Person;
import local.sop.datawarehouse.person.domain.model.PhoneNumberDraft;
import local.sop.datawarehouse.person.domain.model.valueobjects.Email;
import local.sop.datawarehouse.person.domain.model.valueobjects.FirstName;
import local.sop.datawarehouse.person.domain.model.valueobjects.LastName;
import local.sop.datawarehouse.person.domain.model.valueobjects.OrganizationRef;

@Service
public class PersonDomainService implements PersonDomain {

	@Override
	public Person create(
			FirstName firstName,
			LastName lastName,
			Email email,
			OrganizationRef organizationRef,
			List<PhoneNumberDraft> phoneNumbers
	) {

		Person.Builder builder = Person.builder()
				.firstName(firstName)
				.lastName(lastName)
				.email(email)
				.organizationRef(organizationRef);

		if (phoneNumbers != null) {
			for (PhoneNumberDraft phoneNumber : phoneNumbers) {
				builder.addPhoneNumber(phoneNumber.type(), phoneNumber.value());
			}
		}

		return builder.build();
	}
}