package local.sop.sopinfo.messageperson.application.api;

import java.util.List;
import java.util.Optional;

import local.sop.sopinfo.messageperson.application.api.dto.CreateMessagePersonCmd;
import local.sop.sopinfo.messageperson.application.api.dto.CreatedMessagePersonResult;
import local.sop.sopinfo.messageperson.application.api.dto.MessagePersonResponse;
import local.sop.sopinfo.messageperson.application.api.dto.ToggleActivateMessagePersonCmd;
import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;

public interface MessagePersonDirectory {
    CreatedMessagePersonResult create(CreateMessagePersonCmd command);
    Optional<MessagePersonResponse> findById(CompositeKey id);
    MessagePersonResponse toggleActive(ToggleActivateMessagePersonCmd command);
    List<MessagePersonResponse> getByMessageRef(java.util.UUID messageRef);
    List<MessagePersonResponse> getByPersonRef(java.util.UUID personRef);

}
