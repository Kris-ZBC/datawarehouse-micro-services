package local.sop.sopinfo.sop.interfaceadapters.persistence.jpa;

import java.util.UUID;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
 
 
@Component
@Profile("!test")
public class SopDataSeeder implements CommandLineRunner {
 
    private static final Logger log = LoggerFactory.getLogger(SopDataSeeder.class);
 
    private static final UUID ZBC_SOP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
 
    private final SOPSpringDataRepository repository;
 
    public SopDataSeeder(SOPSpringDataRepository repository) {
        this.repository = repository;
    }
 
    @Override
    public void run(String... args) {
        if (repository.existsById(ZBC_SOP_ID)) {
            log.debug("SOP seed data already present - skipping");
            return;
        }
        repository.save(new SOPEntity(ZBC_SOP_ID, "Ringsted Data/IT", "P7-10", "IT Support"));
        log.info("Seeded SOP: Ringsted Data/IT");
    }
}
 