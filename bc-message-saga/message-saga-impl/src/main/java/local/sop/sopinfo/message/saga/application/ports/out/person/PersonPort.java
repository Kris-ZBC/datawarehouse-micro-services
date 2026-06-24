package local.sop.sopinfo.message.saga.application.ports.out.person;

import java.util.UUID;

import local.sop.sopinfo.message.saga.application.api.dto.PersonResponse;

public interface PersonPort {
	PersonResponse findById(UUID id);
}
