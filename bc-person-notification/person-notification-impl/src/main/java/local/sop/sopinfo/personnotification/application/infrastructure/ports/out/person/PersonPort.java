package local.sop.sopinfo.personnotification.application.infrastructure.ports.out.person;

import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.personnotification.application.infrastructure.response.PersonResponse;
import local.sop.sopinfo.sharedkernel.compositekey.validate.CompositeKeyValidator;

public interface PersonPort extends CompositeKeyValidator{
    Optional<PersonResponse> findById(UUID id);
}
