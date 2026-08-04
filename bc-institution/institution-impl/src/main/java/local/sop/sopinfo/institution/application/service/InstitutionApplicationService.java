package local.sop.sopinfo.institution.application.service;

import local.sop.sopinfo.institution.application.api.InstitutionDirectory;
import local.sop.sopinfo.institution.application.api.dto.InstitutionQuery;
import local.sop.sopinfo.institution.application.api.dto.InstitutionResponse;
import local.sop.sopinfo.institution.interfaceadapters.persistence.jpa.InstitutionSpringDataRepository;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException; // Ensure this is the correct path
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class InstitutionApplicationService implements InstitutionDirectory {

    private static final Logger log = LoggerFactory.getLogger(InstitutionApplicationService.class);
    private final InstitutionSpringDataRepository repository;

    public InstitutionApplicationService(InstitutionSpringDataRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    @Override
    public InstitutionResponse findById(InstitutionQuery query) {
        log.info("Searching for Institution record with UUID: {}", query.id());

        return repository.findById(query.id())
                .map(entity -> new InstitutionResponse(
                        entity.getId(),
                        entity.getName(),
                        entity.getAddress()
                ))
                .orElseThrow(() -> {
                    log.warn("Institution record NOT FOUND for UUID: {}", query.id());
                    return new NotFoundException("institution.notFound", Map.of("id", query.id()));
                });
    }
}