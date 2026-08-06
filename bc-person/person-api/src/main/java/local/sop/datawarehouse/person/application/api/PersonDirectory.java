package local.sop.datawarehouse.person.application.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.datawarehouse.person.application.api.dto.AddPhoneNumberCmd;
import local.sop.datawarehouse.person.application.api.dto.CreatePersonCmd;
import local.sop.datawarehouse.person.application.api.dto.PersonResponse;
import local.sop.datawarehouse.person.application.api.dto.PhoneNumberResponse;
import local.sop.datawarehouse.person.application.api.dto.RemovePhoneNumberCmd;
import local.sop.datawarehouse.person.application.api.dto.UpdatePersonCmd;
import local.sop.common.libs.sharedkernel.sagas.compensate.Compensatable;

public interface PersonDirectory extends Compensatable{
    UUID create(CreatePersonCmd cmd);
    Optional<PersonResponse> findById(UUID id);
    List<PersonResponse> findAll();
    List<PersonResponse> searchByName(String name);
	PersonResponse update(UUID id, UpdatePersonCmd cmd);
	PhoneNumberResponse addPhoneNumber(AddPhoneNumberCmd cmd);
	void removePhoneNumber(RemovePhoneNumberCmd cmd);
}