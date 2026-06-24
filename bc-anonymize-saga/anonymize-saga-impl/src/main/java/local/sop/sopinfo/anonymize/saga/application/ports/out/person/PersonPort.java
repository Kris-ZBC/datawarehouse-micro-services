package local.sop.sopinfo.anonymize.saga.application.ports.out.person;

import java.util.UUID;

import local.sop.sopinfo.person.application.api.dto.PersonResponse;

public interface PersonPort {
	PersonResponse get(UUID personRef);
}
