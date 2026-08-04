package local.sop.sopinfo.workhour.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.valueobjects.utils.UUIDUtil;

public class WorkScheduleIdTest {

    @Test
    void happyPath_of_should_return_new_id() {
        assertNotNull(WorkScheduleId.of(UUIDUtil.newUuid()));
        assertNotNull(WorkScheduleId.of("d72df32b-bba2-4ca7-bafd-d60e73e5fa97", "id"));

    }

    @Test
    void happyPath_from_string_should_return_new_id() {
        assertNotNull(WorkScheduleId.fromString("d72df32b-bba2-4ca7-bafd-d60e73e5fa97", "id"));

    }
}
