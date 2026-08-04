package local.sop.sopinfo.messageperson.application.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.compositekey.validate.ValidateCompositeKey;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.sopinfo.messageperson.application.api.MessagePersonDirectory;
import local.sop.sopinfo.messageperson.application.api.dto.CreateMessagePersonCmd;
import local.sop.sopinfo.messageperson.application.api.dto.CreatedMessagePersonResult;
import local.sop.sopinfo.messageperson.application.api.dto.MessagePersonResponse;
import local.sop.sopinfo.messageperson.application.api.dto.ToggleActivateMessagePersonCmd;
import local.sop.sopinfo.messageperson.domain.model.MessagePerson;
import local.sop.sopinfo.messageperson.domain.ports.out.MessagePersonPort;
import local.sop.sopinfo.messageperson.domain.service.MessagePersonDomain;

@Service
public class MessagePersonApplicationService implements MessagePersonDirectory {

    private final MessagePersonPort repository;

    private final MessagePersonDomain domain;

    public MessagePersonApplicationService(MessagePersonPort repository, MessagePersonDomain domain) {
        this.repository = repository;
        this.domain = domain;
    }

    @Override
    @Transactional
    @ValidateCompositeKey(ports = {"messagePort", "personPort"})
    public CreatedMessagePersonResult create(CreateMessagePersonCmd command) {
        MessagePerson aggregate = domain.createMessagePerson(command.id(), command.active());

        MessagePerson saved = repository.save(aggregate);
        return new CreatedMessagePersonResult(saved.getId());

}

    @Override
    @Transactional
    @ValidateCompositeKey(ports = {"messagePort", "personPort"})
    public MessagePersonResponse toggleActive(ToggleActivateMessagePersonCmd command) {

        Optional<MessagePerson> existingOptional = repository.findById(command.id());
        if (existingOptional.isEmpty()) {
            throw new NotFoundException("key.not.found", Map.of("field","id","value",command.id()));
        }
        MessagePerson existing = existingOptional.get();
        MessagePerson updated = domain.toggleActivateMessagePerson(command.id(), command.active(), existing.getCreatedAt().value());


        repository.update(updated);
        return new MessagePersonResponse(updated.getId(), updated.isActive(), updated.getCreatedAt().value());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MessagePersonResponse> findById(CompositeKey id) {
        return Optional.ofNullable(repository.findById(id)
            .map(s -> new MessagePersonResponse(s.getId(), s.isActive(), s.getCreatedAt().value()))
            .orElseThrow(()-> new NotFoundException("key.not.found", Map.of("field","id","value",id))));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessagePersonResponse> getByMessageRef(UUID messageRef) {
        return repository.findByMessageRef(messageRef).stream()
            .map(s -> new MessagePersonResponse(s.getId(), s.isActive(), s.getCreatedAt().value()))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessagePersonResponse> getByPersonRef(UUID personRef) {
        return repository.findByPersonRef(personRef).stream()
            .map(s -> new MessagePersonResponse(s.getId(), s.isActive(), s.getCreatedAt().value()))
            .toList();
    }
}