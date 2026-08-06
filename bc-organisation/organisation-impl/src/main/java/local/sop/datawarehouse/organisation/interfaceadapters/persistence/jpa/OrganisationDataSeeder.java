package local.sop.datawarehouse.organisation.interfaceadapters.persistence.jpa;

import java.util.UUID;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
 
 
@Component
@Profile("!test")
public class OrganisationDataSeeder implements CommandLineRunner {
 
    private static final Logger log = LoggerFactory.getLogger(OrganisationDataSeeder.class);
 
    private static final UUID ZBC_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
 
    private final OrganisationSpringDataRepository repository;
 
    public OrganisationDataSeeder(OrganisationSpringDataRepository repository) {
        this.repository = repository;
    }
 
    @Override
    public void run(String... args) {
        if (repository.existsById(ZBC_ID)) {
            log.debug("Organisation seed data already present - skipping");
            return;
        }
        repository.save(new OrganisationEntity(ZBC_ID, "ZBC", "10521815"));
        log.info("Seeded organisation: ZBC");
    }
}