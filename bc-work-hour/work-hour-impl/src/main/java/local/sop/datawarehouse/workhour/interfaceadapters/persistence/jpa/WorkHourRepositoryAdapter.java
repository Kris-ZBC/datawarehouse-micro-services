package local.sop.datawarehouse.workhour.interfaceadapters.persistence.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.workhour.domain.model.WorkHour;
import local.sop.datawarehouse.workhour.domain.model.valueobjects.WorkHourId;
import local.sop.datawarehouse.workhour.domain.ports.out.WorkHourRepositoryPort;


@Repository
public class WorkHourRepositoryAdapter implements WorkHourRepositoryPort {

    private static final Logger log =
        LoggerFactory.getLogger(WorkHourRepositoryAdapter.class);

    private final WorkHourMapper mapper;
    private final WorkHourSpringDataRepository repository;

    public WorkHourRepositoryAdapter(WorkHourMapper mapper,WorkHourSpringDataRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    @Override
    public WorkHour save(WorkHour workHour) {
        WorkHourEntity entity = repository.save(mapper.toEntity(workHour));

        log.info("Saved WorkHour with id {}", entity.getId());

        return mapper.toDomain(entity);
    }

    @Override
    public WorkHour update(WorkHour workHour) {

        WorkHourEntity existing = repository.findById(workHour.getId().value())
            .orElseThrow(() -> {
                log.warn("WorkHour not found for update: {}", workHour.getId());

                return new ValidationException("workhour.not.found",Map.of("id", workHour.getId().value()));
            });

        mapper.copyIntoEntity(workHour, existing);

        WorkHourEntity saved = repository.save(existing);

        log.info("Updated WorkHour with id {}", saved.getId());

        return mapper.toDomain(saved);
    }

    @Override
    public Optional<WorkHour> findById(WorkHourId id) {

        return repository.findById(id.value())
            .map(mapper::toDomain);
    }

    @Override
    public List<WorkHour> getAll() {

        return repository.findAll().stream()
            .map(mapper::toDomain)
            .toList();
    }
}
