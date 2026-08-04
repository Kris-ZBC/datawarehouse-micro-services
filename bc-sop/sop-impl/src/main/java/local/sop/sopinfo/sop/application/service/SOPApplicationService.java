package local.sop.sopinfo.sop.application.service;

import local.sop.sopinfo.sop.application.api.SOPDirectory;
import local.sop.sopinfo.sop.application.api.dto.SOPQuery;
import local.sop.sopinfo.sop.application.api.dto.SopResponse;
import local.sop.sopinfo.sop.interfaceadapters.persistence.jpa.SOPSpringDataRepository;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Map;
import java.util.Optional;

@Service
public class SOPApplicationService implements SOPDirectory {

    private static final Logger log = LoggerFactory.getLogger(SOPApplicationService.class);
    private final SOPSpringDataRepository repository;

    public SOPApplicationService(SOPSpringDataRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<SopResponse> findById(SOPQuery query) {
        if(query == null || query.uuid() == null) {
            log.warn("Invalid query: {}", query);
            throw new NotFoundException("sop.notFound", Map.of("id", query == null ? "null" : "null"));
        }
        log.info("Searching for SOP record with UUID: {}", query.uuid());

        return Optional.ofNullable(repository.findById(query.uuid()).map(entity -> new SopResponse(entity.getId(), entity.getName(), entity.getAddress(), entity.getEducation())).orElseThrow(() -> {
            log.warn("SOP record not found for UUID: {}", query.uuid());
            throw new NotFoundException("sop.notFound", Map.of("id", query.uuid().toString()));
        }));
    }
}