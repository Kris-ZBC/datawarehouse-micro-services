package local.sop.sopinfo.sopeducation.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sharedkernel.compositekey.validate.ValidateCompositeKey;
import local.sop.sopinfo.sharedkernel.exceptions.NotFoundException;
import local.sop.sopinfo.sopeducation.application.api.SopEducationDirectory;
import local.sop.sopinfo.sopeducation.application.api.dto.CreateSopEducationCmd;
import local.sop.sopinfo.sopeducation.application.api.dto.CreatedSopEducationResult;
import local.sop.sopinfo.sopeducation.application.api.dto.SopEducationResponse;
import local.sop.sopinfo.sopeducation.application.api.dto.ToggleActivateSopEducationCmd;
import local.sop.sopinfo.sopeducation.domain.model.SopEducation;
import local.sop.sopinfo.sopeducation.domain.ports.out.SopEducationPort;
import local.sop.sopinfo.sopeducation.domain.service.SopEducationDomain;

@Service
public class SopEducationApplicationService implements SopEducationDirectory {

    private final SopEducationPort repository;
    private final SopEducationDomain domain;

    public SopEducationApplicationService(SopEducationPort repository, SopEducationDomain domain) {
        this.repository = repository;
        this.domain = domain;
    }

    @Override
    @Transactional
    @ValidateCompositeKey(ports = {"sopPort", "educationPort"})
    public CreatedSopEducationResult create(CreateSopEducationCmd command) {
        SopEducation aggregate = 
                domain.createSopEducation(command.id(), true);

        SopEducation saved = repository.save(aggregate);
        return new CreatedSopEducationResult(saved.getId());
    }

    @Override
    @Transactional
    @ValidateCompositeKey(ports = {"sopPort", "educationPort"})
    public SopEducationResponse toggleActive(ToggleActivateSopEducationCmd command) {
        // Implementation for toggling activation status
        Optional<SopEducation> existingOpt = repository.findById(command.id());
        if (existingOpt.isEmpty()) {
            throw new NotFoundException("key.not.found", Map.of("field", "id", "value", command.id()));
        }
        SopEducation existing = existingOpt.get();
        SopEducation updated = domain.toggleActivateSopEducation(command.id(), !existing.isActive(), existing.getCreatedAt().value());
        repository.update(updated);
        return new SopEducationResponse(updated.getId(), updated.isActive(), updated.getCreatedAt().value());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SopEducationResponse> findById(CompositeKey id) {
        return Optional.ofNullable(repository.findById(id)
                .map(s -> new SopEducationResponse(s.getId(), s.isActive(), s.getCreatedAt().value()))
                .orElseThrow(() -> new NotFoundException("key.not.found", Map.of("field", "id", "value", id))));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SopEducationResponse> getBySopRef(UUID sopRef) {
        return repository.findBySopRef(sopRef).stream()
                .map(s -> new SopEducationResponse(s.getId(), s.isActive(), s.getCreatedAt().value()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SopEducationResponse> getByEducationRef(UUID educationRef) {
        return repository.findByEducationRef(educationRef).stream()
                .map(s -> new SopEducationResponse(s.getId(), s.isActive(), s.getCreatedAt().value()))
                .toList();
    }

}
