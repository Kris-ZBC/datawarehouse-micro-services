package local.sop.datawarehouse.login.interfaceadapters.persistence.jpa;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import local.sop.datawarehouse.sharedlib.enums.LoginStatus;
import local.sop.datawarehouse.sharedlib.login.WellKnownLogins;


@Component 
@Profile("!test") // don't seed in tests
public class LoginDataSeeder implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;

    private final LoginSpringDataRepository loginRepository;

    public LoginDataSeeder(PasswordEncoder passwordEncoder, LoginSpringDataRepository loginRepository) {
        this.passwordEncoder = passwordEncoder;
        this.loginRepository = loginRepository;
    }

    @Override
    public void run(String... args) {
        // CHANGED: was a locally hardcoded copy of this UUID — now the
        // same shared constant TechUserValidatorAdapter and
        // registration-saga both use, so all three can never silently
        // disagree about which Login ID "the tech user" actually is.
        UUID techUserId = WellKnownLogins.TECH_USER_ID;
        if (loginRepository.existsById(techUserId)) {
            return;
        }

        UUID personRef = UUID.fromString("00000000-0000-0000-0000-000000000000"); // DO NOT EXIST!
        String username = "techuser";
        String passwordHash = passwordEncoder.encode("Kode1234!"); // bcrypt hash for "password"

        LoginEntity.Builder builder = new LoginEntity.Builder()
                .id(techUserId)
                .personRef(personRef)
                .username(username)
                .password(passwordHash)
                .status(LoginStatus.ACTIVATED)
                .createdAt(LocalDateTime.now());

        loginRepository.save(builder.build());
    }
}