package local.sop.datawarehouse.message.domain.service;

import java.util.UUID;

import local.sop.datawarehouse.message.domain.model.Message;

public interface MessageDomain {

    Message create(UUID senderPersonRef, String message);
}