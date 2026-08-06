package local.sop.datawarehouse.workhour.interfaceadapters.persistence.jpa;

import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

@Entity
@Table(name = "work_hour")
public class WorkHourEntity {

    @Id
    private UUID id;

    @Version
    private Long version;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    protected WorkHourEntity() {
        // JPA
    }

    private WorkHourEntity(UUID id,LocalTime startTime,LocalTime endTime) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public WorkHourEntity withStartTime(LocalTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public WorkHourEntity withEndTime(LocalTime endTime) {
        this.endTime = endTime;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UUID id;
        private LocalTime startTime;
        private LocalTime endTime;

        public Builder id(UUID id) {
            if (id == null) {
                throw new ValidationException("workhour.id.required",Map.of("field", "id"));
            }

            this.id = id;
            return this;
        }

        public Builder startTime(LocalTime startTime) {
            if (startTime == null) {
                throw new ValidationException("workhour.starttime.required",Map.of("field", "startTime"));
            }

            this.startTime = startTime;
            return this;
        }

        public Builder endTime(LocalTime endTime) {
            if (endTime == null) {
                throw new ValidationException("workhour.endtime.required",Map.of("field", "endTime"));
            }

            this.endTime = endTime;
            return this;
        }

        public WorkHourEntity build() {

        if (id == null) {
            throw new ValidationException("workhour.id.required",Map.of("field", "id"));
        }

        if (startTime == null) {
            throw new ValidationException("workhour.starttime.required",Map.of("field", "startTime"));
        }

        if (endTime == null) {
            throw new ValidationException("workhour.endtime.required",Map.of("field", "endTime"));
        }

        if (!endTime.isAfter(startTime)) {
            throw new ValidationException("workhour.timeframe.invalid",Map.of("startTime", startTime.toString(),"endTime", endTime.toString()));
        }

        return new WorkHourEntity(id, startTime, endTime);
        }
    }
}