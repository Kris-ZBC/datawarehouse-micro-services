package local.sop.datawarehouse.personnotification.application.infrastructure.ports.out.person;

import java.util.Optional;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.compositekey.validate.CompositeKeyValidator;
import local.sop.datawarehouse.personnotification.application.infrastructure.response.PersonResponse;

public interface PersonPort extends CompositeKeyValidator{
    Optional<PersonResponse> findById(UUID id);
}
