package local.sop.sopinfo.person.application.mapper;

import local.sop.sopinfo.person.application.api.dto.PersonResponse;
import local.sop.sopinfo.person.application.api.dto.PhoneNumberResponse;
import local.sop.sopinfo.person.domain.model.Person;
import local.sop.sopinfo.person.domain.model.PhoneNumber;

public final class PersonResponseMapper {

	private PersonResponseMapper() {
	}

	public static PersonResponse toResponse(Person person) {
		return new PersonResponse(
				person.getId().value(),
				person.getFirstName().value(),
				person.getLastName().value(),
				person.getEmail().value(),
				person.getOrganizationRef().value(),
				person.getPhoneNumbers().stream()
						.map(PersonResponseMapper::toResponse)
						.toList());
	}

	public static PhoneNumberResponse toResponse(PhoneNumber phoneNumber) {
		return new PhoneNumberResponse(
				phoneNumber.getId().value(),
				phoneNumber.getType(),
				phoneNumber.getValue().value());
	}
}