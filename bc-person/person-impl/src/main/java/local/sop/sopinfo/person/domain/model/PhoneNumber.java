package local.sop.sopinfo.person.domain.model;

import java.util.StringJoiner;
import java.util.Map;

import local.sop.sopinfo.person.domain.model.valueobjects.PhoneNumberId;
import local.sop.sopinfo.person.domain.model.valueobjects.PhoneNumberValue;
import local.sop.sopinfo.sharedkernel.enums.PhoneUserType;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;


public final class PhoneNumber {

	private final DomainId id;
	private final PhoneUserType type;
	private final PhoneNumberValue value;

	public PhoneNumber(DomainId id, PhoneUserType type, PhoneNumberValue value) {
		this.id = id;
		this.type = type;
		this.value = value;	
	}

	public PhoneNumber withType(PhoneUserType type) {
		return new PhoneNumber(this.id, type, this.value);
	}

	public PhoneNumber withValue(PhoneNumberValue value) {
		return new PhoneNumber(this.id, this.type, value);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", getClass().getSimpleName() + "{", "}")
				.add("id        = " + (id == null ? null : id.toString())).add(String.valueOf('\n'))
				.add("type = " + (type == null ? null : type.toString())).add(String.valueOf('\n'))
				.add("value  = " + (value == null ? null : value.value())).add(String.valueOf('\n'))
				.add(String.valueOf('\n'))
				.toString();
	}

	public static Builder builder() { return new Builder(); }

	public static class Builder {
		private DomainId id;
		private PhoneUserType type;
		private PhoneNumberValue value;

		public Builder id(DomainId v) {this.id = v; return this;}
		public Builder type(PhoneUserType v) {this.type = v; return this;}
		public Builder value(PhoneNumberValue v) {this.value = v; return this;}

        public PhoneNumber build() {
            if(id == null) this.id = PhoneNumberId.newId();
            if(type == null) throw new ValidationException("phoneNumber.type.blank", Map.of("field", "type"));
            if(value == null) throw new ValidationException("phoneNumber.value.blank", Map.of("field", "value"));

            return new PhoneNumber(id, type, value);
        }
	}

	// Getters (no setters)
	public DomainId getId() { return id; }
	public PhoneUserType getType() { return type; }
	public PhoneNumberValue getValue() { return value; }
}