package local.sop.sopinfo.workhour.domain.model;

import java.time.LocalTime;
import java.util.Map;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.workhour.domain.model.valueobjects.WorkHourId;

public class WorkHour {

    private final WorkHourId id;
    private final LocalTime startTime;
    private final LocalTime endTime;

    private WorkHour(WorkHourId id, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public WorkHour withStartTime(LocalTime startTime) {
        return new WorkHour(this.id, startTime, this.endTime);
    }

    public WorkHour withEndTime(LocalTime endTime) {
        return new WorkHour(this.id, this.startTime, endTime);
    }
    
    public WorkHourId getId() {
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
        private WorkHourId id;
        private LocalTime startTime;
        private LocalTime endTime;

        public Builder id(WorkHourId id) {
            this.id = id;
            return this;
        }

        public Builder startTime(LocalTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(LocalTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public WorkHour build() {

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

            return new WorkHour(this.id,this.startTime,this.endTime);
        }
    }
}

