package local.sop.sopinfo.personnotification.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import local.sop.sopinfo.personnotification.application.api.PersonNotificationDirectory;
import local.sop.sopinfo.personnotification.application.api.dto.CreatePersonNotificationCmd;
import local.sop.sopinfo.personnotification.application.api.dto.CreatedPersonNotificationResult;
import local.sop.sopinfo.personnotification.application.api.dto.PersonNotificationResponse;
import local.sop.sopinfo.personnotification.application.api.dto.ToggleActivatePersonNotificationCmd;
import local.sop.sopinfo.personnotification.domain.model.PersonNotification;
import local.sop.sopinfo.personnotification.domain.ports.out.PersonNotificationPort;
import local.sop.sopinfo.personnotification.domain.service.PersonNotificationDomain;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.compositekey.validate.ValidateCompositeKey;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;

@Service
@Transactional
public class PersonNotificationApplicationService implements PersonNotificationDirectory {


    private final PersonNotificationDomain domain;
    private final PersonNotificationPort repository;

    public PersonNotificationApplicationService(PersonNotificationDomain domain, PersonNotificationPort repository) {
        this.domain = domain;
        this.repository = repository;
    }

    @Override
    @Transactional
    @ValidateCompositeKey(ports = {"personPort", "notificationPort"})
    public CreatedPersonNotificationResult create(CreatePersonNotificationCmd command) {
        PersonNotification aggregate = domain.createPersonNotification(command.id(), command.active());

        PersonNotification saved = repository.save(aggregate);
        return new CreatedPersonNotificationResult(saved.getId());
    }

    @Override
    @Transactional
    @ValidateCompositeKey(ports = {"personPort", "notificationPort"})
    public PersonNotificationResponse toggleActive(ToggleActivatePersonNotificationCmd command) {

        Optional<PersonNotification> existingOptional = repository.findById(command.id());
        if (existingOptional.isEmpty()) {
            throw new NotFoundException("key.not.found", Map.of("field","id","value",command.id()));
        }
        PersonNotification existing = existingOptional.get();
        PersonNotification updated = domain.toggleActivatePersonNotification(command.id(), command.active(), existing.getCreatedAt().value());

        repository.update(updated);
        return new PersonNotificationResponse(updated.getId(), updated.isActive(), updated.getCreatedAt().value());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PersonNotificationResponse> findById(CompositeKey id) {
        return Optional.ofNullable(repository.findById(id)
            .map(s -> new PersonNotificationResponse(s.getId(), s.isActive(), s.getCreatedAt().value()))
            .orElseThrow(()-> new NotFoundException("key.not.found", Map.of("field","id","value",id))));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonNotificationResponse> getByPersonRef(UUID personRef) {
        return repository.findByPersonRef(personRef).stream()
            .map(s -> new PersonNotificationResponse(s.getId(), s.isActive(), s.getCreatedAt().value()))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonNotificationResponse> getByNotificationRef(UUID notificationRef) {
        return repository.findByNotificationRef(notificationRef).stream()
            .map(s -> new PersonNotificationResponse(s.getId(), s.isActive(), s.getCreatedAt().value()))
            .toList();
    }

}
