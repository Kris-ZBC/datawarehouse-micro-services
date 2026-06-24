package local.sop.sopinfo.sopeducation.domain.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sopeducation.domain.model.SopEducation;

public interface SopEducationPort {
    Optional<SopEducation> findById(CompositeKey id);
    SopEducation save(SopEducation s);
    void update(SopEducation s);
    List<SopEducation> findBySopRef(UUID sopRef);
    List<SopEducation> findByEducationRef(UUID educationId);
}
