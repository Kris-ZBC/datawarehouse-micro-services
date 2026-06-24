package local.sop.sopinfo.sopinstructor.application.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sharedkernel.compositekey.validate.ValidateCompositeKey;
import local.sop.sopinfo.sharedkernel.exceptions.NotFoundException;
import local.sop.sopinfo.sopinstructor.application.api.SopInstructorDirectory;
import local.sop.sopinfo.sopinstructor.application.api.dto.CreateSopInstructorCmd;
import local.sop.sopinfo.sopinstructor.application.api.dto.CreatedSopInstructorResult;
import local.sop.sopinfo.sopinstructor.application.api.dto.SopInstructorResponse;
import local.sop.sopinfo.sopinstructor.application.api.dto.ToggleActivateSopInstructorCmd;
import local.sop.sopinfo.sopinstructor.domain.model.SopInstructor;
import local.sop.sopinfo.sopinstructor.domain.ports.out.SopInstructorPort;
import local.sop.sopinfo.sopinstructor.domain.service.SopInstructorDomain;

@Service
public class SopInstructorApplicationService implements SopInstructorDirectory {

    private final SopInstructorPort repository;

    private final SopInstructorDomain domain;

    public SopInstructorApplicationService(SopInstructorPort repository, SopInstructorDomain domain) {
        this.repository = repository;
        this.domain = domain;
    }

    @Override
    @Transactional
    @ValidateCompositeKey(ports = {"sopPort", "instructorPort"})
    public CreatedSopInstructorResult create(CreateSopInstructorCmd command) {
        SopInstructor aggregate = domain.createSopInstructor(command.id(), command.active());

        SopInstructor saved = repository.save(aggregate);
        return new CreatedSopInstructorResult(saved.getId());

}

    @Override
    @Transactional
    @ValidateCompositeKey(ports = {"sopPort", "instructorPort"})
    public SopInstructorResponse toggleActive(ToggleActivateSopInstructorCmd command) {

        Optional<SopInstructor> existingOptional = repository.findById(command.id());
        if (existingOptional.isEmpty()) {
            throw new NotFoundException("key.not.found", Map.of("field","id","value",command.id()));
        }
        SopInstructor existing = existingOptional.get();
        SopInstructor updated = domain.toggleActivateSopInstructor(command.id(), command.active(), existing.getCreatedAt().value());


        repository.update(updated);
        return new SopInstructorResponse(updated.getId(), updated.isActive(), updated.getCreatedAt().value());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SopInstructorResponse> findById(CompositeKey id) {
        return Optional.ofNullable(repository.findById(id)
            .map(s -> new SopInstructorResponse(s.getId(), s.isActive(), s.getCreatedAt().value()))
            .orElseThrow(()-> new NotFoundException("key.not.found", Map.of("field","id","value",id))));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SopInstructorResponse> getBySopRef(UUID sopRef) {
        return repository.findBySopRef(sopRef).stream()
            .map(s -> new SopInstructorResponse(s.getId(), s.isActive(), s.getCreatedAt().value()))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SopInstructorResponse> getByInstructorRef(UUID instructorRef) {
        return repository.findByInstructorRef(instructorRef).stream()
            .map(s -> new SopInstructorResponse(s.getId(), s.isActive(), s.getCreatedAt().value()))
            .toList();
    }
}