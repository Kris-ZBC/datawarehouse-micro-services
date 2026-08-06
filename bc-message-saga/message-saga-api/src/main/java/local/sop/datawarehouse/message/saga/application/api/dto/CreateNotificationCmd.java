package local.sop.datawarehouse.message.saga.application.api.dto;

import java.util.UUID;

public record CreateNotificationCmd(UUID messageRef) {}
