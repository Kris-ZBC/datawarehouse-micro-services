package local.sop.datawarehouse.anonymize.saga.application.ports.out.person;

import java.util.UUID;

import local.sop.datawarehouse.person.application.api.dto.PersonResponse;

public interface PersonPort {
	PersonResponse get(UUID personRef);
}
