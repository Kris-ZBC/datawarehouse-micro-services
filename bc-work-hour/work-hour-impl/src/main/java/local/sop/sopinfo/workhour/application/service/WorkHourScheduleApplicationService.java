package local.sop.sopinfo.workhour.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import local.sop.sopinfo.sharedkernel.enums.WeekDay;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.workhour.application.api.WorkHourDirectory;
import local.sop.sopinfo.workhour.application.api.dto.CreateWorkHourScheduleCmd;
import local.sop.sopinfo.workhour.application.api.dto.FindByScheduleIdQuery;
import local.sop.sopinfo.workhour.application.api.dto.FindByScheduleParamsQuery;
import local.sop.sopinfo.workhour.application.api.dto.UpdateWorkHourScheduleCmd;
import local.sop.sopinfo.workhour.application.api.dto.WorkScheduleResponse;
import local.sop.sopinfo.workhour.domain.ports.out.WorkHourScheduleRepositoryPort;
import local.sop.sopinfo.workhour.domain.service.WorkHourScheduleDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WorkHourScheduleApplicationService implements WorkHourDirectory {

    private final WorkHourScheduleRepositoryPort schedules;
    private final WorkHourScheduleDomain domain;
    private static final Logger log = LoggerFactory.getLogger(WorkHourScheduleApplicationService.class);


    WorkHourScheduleApplicationService(WorkHourScheduleRepositoryPort schedules, WorkHourScheduleDomain domain) {
        this.schedules = schedules;
        this.domain = domain;
    }

    @Transactional
    @Override
    public UUID create(CreateWorkHourScheduleCmd cmd) {
        /* Ask domain servcie first*/
        try {
            var whs = domain.create(cmd.startTime(), cmd.endTime(), WeekDay.parse(cmd.weekDay()), cmd.sopRef());
            whs = schedules.save(whs);
            log.info("Work Hour Schedule created {}", whs);
            return whs.getId().value();
        }
        catch(ValidationException ex) {
            // Lad validation exceptions propagate ubeskåret
            throw ex;
        }
        catch(RuntimeException ex) {
            log.error("Error in create", ex);
            throw new ValidationException("work_hour.create.failed", Map.of("function", "create"));
        }
        
    }

    @Transactional
    @Override
    public void update(UpdateWorkHourScheduleCmd cmd) {
        try {
            var whs = domain.update(cmd.id(), cmd.startTime(), cmd.endTime(), WeekDay.parse(cmd.weekDay()), cmd.sopRef());
            schedules.update(whs);
            log.info("Work Hour Schedule updated {}", whs);
        }
        catch(RuntimeException ex) {
            log.warn("Error in update", ex);
            throw new ValidationException("work_hour.update.failed", Map.of("function", "update"));
        }
    }

    @Transactional(readOnly=true)
    @Override
    public Optional<WorkScheduleResponse> readById(FindByScheduleIdQuery query) {
        if(query.id() == null) throw new ValidationException("key.required", Map.of("function", "readById"));
        return schedules.findById(query.id())
            .map(whs -> new WorkScheduleResponse(whs.getId().value(), whs.getStartTime().time(), whs.getEndTime().time(), whs.getWeekDay(), whs.getSopRef().value()));
    }

    @Transactional(readOnly=true)
    @Override
    public List<WorkScheduleResponse> readByParams(FindByScheduleParamsQuery query) {
       return schedules.findBySearchParams(query.id(), query.startTime(), query.endTime(), query.weekday(), query.sofRef()).stream()
            .map(whl -> new WorkScheduleResponse(whl.getId().value(), whl.getStartTime().time(), whl.getEndTime().time(), whl.getWeekDay(), whl.getSopRef().value())).toList();
    }

    @Transactional
    @Override
    public void delete(FindByScheduleIdQuery query) {
       var wh = domain.delete(query.id());
       schedules.delete(wh);

    }

}
