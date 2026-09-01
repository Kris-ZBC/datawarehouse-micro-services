package local.sop.datawarehouse.login.application.service.util;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class PasswordGenerator {
	private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
	private static final String DIGIT = "0123456789";
	private static final String SPECIAL = "!@#$%^&*()_+-=[]{};':\"\\\\|,.<>/?";

	private static final String ALL = UPPER + LOWER + DIGIT + SPECIAL;

	private static final int LENGTH = 12;

	private final SecureRandom random = new SecureRandom();

	public String generate() {
		char[] password = new char[LENGTH];
		password[0] = randomChar(UPPER);
		password[1] = randomChar(LOWER);
		password[2] = randomChar(DIGIT);
		password[3] = randomChar(SPECIAL);

		for (int i = 4; i < LENGTH; i++) {
			password[i] = randomChar(ALL);
		}

		// Shuffle to avoid predictable character-class ordering
		for (int i = LENGTH - 1; i > 0; i--) {
			int j = random.nextInt(i + 1);
			char tmp = password[i];
			password[i] = password[j];
			password[j] = tmp;
		}

		return new String(password);
	}

	private char randomChar(String source) {
		return source.charAt(random.nextInt(source.length()));
	}
}
