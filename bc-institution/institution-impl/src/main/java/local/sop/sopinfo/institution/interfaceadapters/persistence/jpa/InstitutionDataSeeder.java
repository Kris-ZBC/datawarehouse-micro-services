package local.sop.sopinfo.institution.interfaceadapters.persistence.jpa;

import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test") // don't seed in tests
public class InstitutionDataSeeder implements CommandLineRunner {

    private final InstitutionSpringDataRepository institutionRepository;

    public InstitutionDataSeeder(InstitutionSpringDataRepository institutionRepository) {
        this.institutionRepository = institutionRepository;
    }

    @Override
    public void run(String... args) {
        UUID zbcId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        if (institutionRepository.existsById(zbcId)) {
            return;
        }
        institutionRepository.save(new InstitutionEntity(zbcId, "ZBC Ringsted", "Ahorn Alle 5, Ringsted"));
    }
}



