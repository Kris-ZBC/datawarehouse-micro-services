package local.sop.sopinfo.message.domain.service;

import java.util.UUID;

import local.sop.sopinfo.message.domain.model.Message;

public interface MessageDomain {

    Message create(UUID senderPersonRef, String message);
}