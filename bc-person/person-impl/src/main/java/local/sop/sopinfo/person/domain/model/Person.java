package local.sop.sopinfo.person.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.Map;

import local.sop.sopinfo.person.domain.model.valueobjects.Email;
import local.sop.sopinfo.person.domain.model.valueobjects.FirstName;
import local.sop.sopinfo.person.domain.model.valueobjects.LastName;
import local.sop.sopinfo.person.domain.model.valueobjects.OrganizationRef;
import local.sop.sopinfo.person.domain.model.valueobjects.PersonId;
import local.sop.sopinfo.person.domain.model.valueobjects.PhoneNumberId;
import local.sop.sopinfo.person.domain.model.valueobjects.PhoneNumberValue;
import local.sop.sopinfo.sharedkernel.enums.PhoneUserType;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;

public final class Person {
	private final DomainId id;
	private final FirstName firstName;
	private final LastName lastName;
	private final Email email;
	private final OrganizationRef organizationRef;

	private final List<PhoneNumber> phoneNumbers;

	private Person(PersonId id, FirstName firstName, LastName lastName, Email email, OrganizationRef organizationRef) {
		this(id, firstName, lastName, email, organizationRef, List.of());
	}

	private Person(DomainId id, FirstName firstName, LastName lastName, Email email, OrganizationRef organizationRef, List<PhoneNumber> phoneNumbers) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.organizationRef = organizationRef;
		this.phoneNumbers = phoneNumbers == null ? List.of() : List.copyOf(phoneNumbers);
	}

	public Person withFirstName(FirstName firstName) {
		return new Person(this.id, firstName, this.lastName, this.email, this.organizationRef, this.phoneNumbers);
	}

	public Person withLastName(LastName lastName) {
		return new Person(this.id, this.firstName, lastName, this.email, this.organizationRef, this.phoneNumbers);
	}

	public Person withEmail(Email email) {
		return new Person(this.id, this.firstName, this.lastName, email, this.organizationRef, this.phoneNumbers);
	}

	public Person withOrganizationRef(OrganizationRef organizationRef) {
		return new Person(this.id, this.firstName, this.lastName, this.email, organizationRef, this.phoneNumbers);
	}

	public Person withAddedPhoneNumber(PhoneNumber phoneNumber) {
		List<PhoneNumber> copy = new ArrayList<>(this.phoneNumbers);
		copy.add(phoneNumber);
		return new Person(this.id, this.firstName, this.lastName, this.email, this.organizationRef, copy);
	}

	public Person withRemovedPhoneNumber(DomainId phoneNumberId) {
		List<PhoneNumber> copy = this.phoneNumbers.stream()
				.filter(phoneNumber -> !phoneNumber.getId().equals(phoneNumberId))
				.toList();

		return new Person(this.id, this.firstName, this.lastName, this.email, this.organizationRef, copy);
	}

	public boolean hasPhoneNumber(DomainId phoneNumberId) {
		return this.phoneNumbers.stream()
				.anyMatch(phoneNumber -> phoneNumber.getId().equals(phoneNumberId));
	}

	public static Builder builder() { return new Builder(); }

	public static class Builder {
		private PersonId id;
		private FirstName firstName;
		private LastName lastName;
		private Email email;
		private OrganizationRef organizationRef;
		private final List<PhoneNumber> phoneNumbers = new ArrayList<>();

		public Builder id(PersonId value) {this.id = value;return this;}
		public Builder firstName(FirstName value) {this.firstName = value;return this;}
		public Builder lastName(LastName value) {this.lastName = value;return this;}
		public Builder email(Email value) {this.email = value;return this;}
		public Builder organizationRef(OrganizationRef value) {this.organizationRef = value;return this;}
		public Builder phoneNumber(PhoneNumber phoneNumber) {this.phoneNumbers.add(phoneNumber);return this;}
		public Builder addPhoneNumber(PhoneUserType type, PhoneNumberValue value) {
			this.phoneNumbers.add(new PhoneNumber(PhoneNumberId.newId(), type, value));
			return this;
		}

		public Person build() {
			if (id == null) {this.id = PersonId.newId();}
			if (firstName == null) {throw new ValidationException("person.firstname.blank", Map.of("field", "firstName"));}
			if (lastName == null) {throw new ValidationException("person.lastname.blank", Map.of("field", "lastName"));}
			if (email == null) {throw new ValidationException("person.email.blank", Map.of("field", "email"));}
			if (organizationRef == null) {throw new ValidationException("person.organizationref.blank", Map.of("field", "organizationRef"));}

			return new Person(id, firstName, lastName, email, organizationRef, phoneNumbers);
		}
	}

	// Getters (no setters)
	public DomainId getId() {return id;}
	public FirstName getFirstName() {return firstName;}
	public LastName getLastName() {return lastName;}
	public Email getEmail() {return email;}
	public OrganizationRef getOrganizationRef() {return organizationRef;}
	public List<PhoneNumber> getPhoneNumbers() {return phoneNumbers;}

	@Override
	public String toString() {
		return new StringJoiner(", ", getClass().getSimpleName() + "{", "}")
				.add("id        = " + (id == null ? null : id.toString())).add(String.valueOf('\n'))
				.add("firstName = " + (firstName == null ? null : firstName.value())).add(String.valueOf('\n'))
				.add("lastName  = " + (lastName == null ? null : lastName.value())).add(String.valueOf('\n'))
				.add("email     = " + (email == null ? null : email.value())).add(String.valueOf('\n'))
				.add("organizationRef     = " + (organizationRef == null ? null : organizationRef.toString()))
				.add("NumberOfPhoneNumbers     = " + (phoneNumbers == null ? null : phoneNumbers.size()))
				.add(String.valueOf('\n'))
				.toString();
	}
}
