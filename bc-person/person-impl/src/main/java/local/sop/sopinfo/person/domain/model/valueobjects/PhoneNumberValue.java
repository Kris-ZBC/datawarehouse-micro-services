package local.sop.sopinfo.person.domain.model.valueobjects;

import java.util.Map;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public record PhoneNumberValue(String value) {

    private static final int MIN_DIGITS = 7;
    private static final int MAX_DIGITS = 15;

    public PhoneNumberValue {
        if (value == null || value.isBlank()) {
            throw new ValidationException("person.phonenumber.blank", Map.of("field", "phoneNumber"));
        }

        value = value.trim();
		
		// Only preserve '+' when it is the very first character.
		// This keeps normalization deterministic and avoids accepting formatted noise as country prefix.
        boolean hasPlus = value.startsWith("+");
        String digitsOnly = value.replaceAll("\\D", "");

        if (digitsOnly.isEmpty()) {
            throw new ValidationException("person.phonenumber.digitsRequired", Map.of("field", "phoneNumber"));
        }

        if (digitsOnly.length() < MIN_DIGITS) {
            throw new ValidationException(
                    "person.phonenumber.minLength",
                    Map.of("field", "phoneNumber", "min", MIN_DIGITS));
        }

        if (digitsOnly.length() > MAX_DIGITS) {
            throw new ValidationException(
                    "person.phonenumber.maxLength",
                    Map.of("field", "phoneNumber", "max", MAX_DIGITS));
        }

        value = (hasPlus ? "+" : "") + digitsOnly;
    }
}
