package local.sop.datawarehouse.messageperson.application.api;

import java.util.List;
import java.util.Optional;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.datawarehouse.messageperson.application.api.dto.CreateMessagePersonCmd;
import local.sop.datawarehouse.messageperson.application.api.dto.CreatedMessagePersonResult;
import local.sop.datawarehouse.messageperson.application.api.dto.MessagePersonResponse;
import local.sop.datawarehouse.messageperson.application.api.dto.ToggleActivateMessagePersonCmd;

public interface MessagePersonDirectory {
    CreatedMessagePersonResult create(CreateMessagePersonCmd command);
    Optional<MessagePersonResponse> findById(CompositeKey id);
    MessagePersonResponse toggleActive(ToggleActivateMessagePersonCmd command);
    List<MessagePersonResponse> getByMessageRef(java.util.UUID messageRef);
    List<MessagePersonResponse> getByPersonRef(java.util.UUID personRef);

}
