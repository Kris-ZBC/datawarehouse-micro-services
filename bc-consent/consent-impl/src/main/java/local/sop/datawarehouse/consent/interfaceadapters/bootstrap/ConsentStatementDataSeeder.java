package local.sop.datawarehouse.consent.interfaceadapters.bootstrap;

import java.util.UUID;
 
import org.springframework.context.annotation.Profile;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
 
import local.sop.datawarehouse.sharedlib.enums.ConsentPurpose;
import local.sop.datawarehouse.sharedlib.enums.ConsentType;
import local.sop.datawarehouse.consent.domain.model.ConsentStatement;
import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentStatementRef;
import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentStatementValue;
import local.sop.datawarehouse.consent.domain.ports.out.ConsentStatementRepositoryPort;

@Component
@Profile("!test") // don't seed in tests
public class ConsentStatementDataSeeder implements CommandLineRunner {
 
    private final ConsentStatementRepositoryPort consentStatementRepository;
 
    public ConsentStatementDataSeeder(ConsentStatementRepositoryPort consentStatementRepository) {
        this.consentStatementRepository = consentStatementRepository;
    }
 
    @Override
    public void run(String... args) {
        seedIfMissing(
                WellKnownConsentStatements.TERMS_AND_CONDITIONS,
                "I agree to the Terms & Conditions",
                ConsentPurpose.REQUIRED_SERVICE,
                ConsentType.REQUIRED);
 
        seedIfMissing(
                WellKnownConsentStatements.ALLOW_NOTIFICATIONS,
                "I agree to receive notifications",
                ConsentPurpose.COMMUNICATION,
                ConsentType.OPTIONAL);
    }
 
    private void seedIfMissing(UUID id, String statementText, ConsentPurpose purpose, ConsentType type) {
        ConsentStatementRef ref = new ConsentStatementRef(id);
 
        // No existsById() on the port (unlike the Spring Data
        // repository) — findById().isPresent() is the domain-level
        // equivalent.
        if (consentStatementRepository.findById(ref).isPresent()) {
            return;
        }
 
        ConsentStatement statement = ConsentStatement.builder()
                .id(ref)
                .statementText(new ConsentStatementValue(statementText))
                .purpose(purpose)
                .type(type)
                .active(true)
                .build();
 
        consentStatementRepository.save(statement);
    }
}