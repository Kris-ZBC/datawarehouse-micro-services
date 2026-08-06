package local.sop.datawarehouse.workhour.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import local.sop.datawarehouse.workhour.application.api.WorkHourDirectory;
import local.sop.datawarehouse.workhour.application.api.dto.CreateWorkHourCmd;
import local.sop.datawarehouse.workhour.application.api.dto.CreatedWorkHourResult;
import local.sop.datawarehouse.workhour.application.api.dto.UpdateWorkHourCmd;
import local.sop.datawarehouse.workhour.application.api.dto.WorkHourResponse;
import local.sop.datawarehouse.workhour.domain.model.WorkHour;
import local.sop.datawarehouse.workhour.domain.model.valueobjects.WorkHourId;
import local.sop.datawarehouse.workhour.domain.ports.out.WorkHourRepositoryPort;
import local.sop.datawarehouse.workhour.domain.service.WorkHourDomain;

@Service
public class WorkHourApplicationService implements WorkHourDirectory {
        
        private final WorkHourRepositoryPort repository;
        private final WorkHourDomain domain;

        private static final Logger log = LoggerFactory.getLogger(WorkHourApplicationService.class);

        public WorkHourApplicationService(WorkHourRepositoryPort repository,WorkHourDomain domain) {
        this.repository = repository;
        this.domain = domain;
        }

        @Override
        @Transactional
        public CreatedWorkHourResult create(CreateWorkHourCmd command) {

        log.info("Creating WorkHour with startTime: {}, endTime: {}",command.startTime(),command.endTime());

        WorkHour aggregate = domain.create(command.startTime(),command.endTime());

        WorkHour saved = repository.save(aggregate);

        log.info("Successfully created WorkHour with id: {}", saved.getId());

        return new CreatedWorkHourResult(saved.getId().value());
        }

        @Override
        @Transactional
        public WorkHourResponse update(UpdateWorkHourCmd command) {

        log.info("Updating WorkHour with id: {}, startTime: {}, endTime: {}", command.id(), command.startTime(),command.endTime());

        WorkHour updated = domain.update(WorkHourId.of(command.id()), command.startTime(), command.endTime());

        repository.update(updated);

        log.info("Successfully updated WorkHour with id: {}",updated.getId());

        return new WorkHourResponse(updated.getId().value(), updated.getStartTime(), updated.getEndTime());
        }

        @Override
        @Transactional(readOnly = true)
        public Optional<WorkHourResponse> findById(UUID id) {

        log.info("Finding WorkHour with id: {}", id);

        return repository.findById(WorkHourId.of(id))
                .map(workHour -> new WorkHourResponse(workHour.getId().value(), workHour.getStartTime(), workHour.getEndTime()));
        }

        @Override
        @Transactional(readOnly = true)
        public List<WorkHourResponse> getAll() {

        log.info("Retrieving all WorkHours");

        List<WorkHourResponse> result = repository.getAll().stream()
                .map(workHour -> new WorkHourResponse(workHour.getId().value(), workHour.getStartTime(), workHour.getEndTime()))
                .toList();

        log.info("Retrieved {} WorkHours", result.size());

        return result;
        }
}
