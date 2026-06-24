package local.sop.sopinfo.workhour.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import local.sop.sopinfo.sharedkernel.enums.WeekDay;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.sharedkernel.valueobjects.utils.UUIDUtil;
import local.sop.sopinfo.workhour.application.api.WorkHourDirectory;
import local.sop.sopinfo.workhour.application.api.dto.CreateWorkHourScheduleCmd;
import local.sop.sopinfo.workhour.application.api.dto.FindByScheduleIdQuery;
import local.sop.sopinfo.workhour.application.api.dto.FindByScheduleParamsQuery;
import local.sop.sopinfo.workhour.application.api.dto.UpdateWorkHourScheduleCmd;
import local.sop.sopinfo.workhour.domain.model.WorkHourSchedule;
import local.sop.sopinfo.workhour.domain.model.valueobjects.SopRef;
import local.sop.sopinfo.workhour.domain.model.valueobjects.WorkScheduleId;
import local.sop.sopinfo.workhour.domain.model.valueobjects.WorkScheduleTime;
import local.sop.sopinfo.workhour.domain.ports.out.WorkHourScheduleRepositoryPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=none",
    "bc.qualifier=work_hour"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class WorkHourScheduleApplicationServiceTest {
    private ReloadableResourceBundleMessageSource ms;
    private final Locale DA = Locale.forLanguageTag("da");

    @BeforeEach
    public void setup() {
        var r = new ReloadableResourceBundleMessageSource();
        r.setBasenames("classpath:i18n/common-web-messages", "classpath:i18n/common-security-messages", 
            "classpath:i18n/common-data-messages", "classpath:i18n/common-core-messages", "classpath:i18n/work-hour-messages");
        r.setDefaultEncoding("UTF-8");
        r.setFallbackToSystemLocale(false);
        r.setUseCodeAsDefaultMessage(false);
        r.setCacheMillis(-1);
        ms = r;

    }    
    

    @Autowired
    private WorkHourDirectory directory;

    @MockitoBean
    private WorkHourScheduleRepositoryPort schedules;

    @Test
    void happyPath_create_shouldCreateNewObject() {
         WorkHourSchedule whs = WorkHourSchedule.builder()
            .id(WorkScheduleId.of(UUIDUtil.newUuid()))
            .startTime(WorkScheduleTime.of("07:00"))
            .endTime(WorkScheduleTime.of("16:00"))
            .weekDay(WeekDay.MONDAY)
            .sopRef(SopRef.of(UUIDUtil.newUuid()))
            .build();
                
        when(schedules.save(Mockito.any())).thenReturn(whs);

        var id = directory.create(new CreateWorkHourScheduleCmd("07:45", "15:45", "MONDAY", UUID.randomUUID()));
        assertNotNull(id);
        assertEquals(whs.getId().value(), id);
        Mockito.verify(schedules, Mockito.times(1)).save(Mockito.any());
        Mockito.verifyNoMoreInteractions(schedules);

    }


    @Test
    void happyPath_update_shouldUpdateObject() {
        WorkHourSchedule whs = WorkHourSchedule.builder()
            .id(WorkScheduleId.of(UUIDUtil.newUuid()))
            .startTime(WorkScheduleTime.of("07:00"))
            .endTime(WorkScheduleTime.of("16:00"))
            .weekDay(WeekDay.MONDAY)
            .sopRef(SopRef.of(UUIDUtil.newUuid()))
            .build();

        Mockito.doNothing().when(schedules).update(Mockito.any());

        directory.update(new UpdateWorkHourScheduleCmd(whs.getId().value(), whs.getStartTime().time(), whs.getEndTime().time(), "TUESDAY", whs.getSopRef().value()));

        Mockito.verify(schedules, Mockito.times(1)).update(Mockito.any());
        Mockito.verifyNoMoreInteractions(schedules);


    }

    @Test
    void happyPath_delete_shouldDeleteById() {
        UUID id = UUIDUtil.newUuid();

        directory.delete(new FindByScheduleIdQuery(id));

        Mockito.verify(schedules, Mockito.times(1)).delete(Mockito.argThat(wh ->
            wh != null && id.equals(wh.getId().value())
        ));
        Mockito.verifyNoMoreInteractions(schedules);
    }

    @Test
    void happyPath_readById_ShouldReturnNewObject() {
        WorkHourSchedule whs = WorkHourSchedule.builder()
            .id(WorkScheduleId.of(UUIDUtil.newUuid()))
            .startTime(WorkScheduleTime.of("07:00"))
            .endTime(WorkScheduleTime.of("16:00"))
            .weekDay(WeekDay.MONDAY)
            .sopRef(SopRef.of(UUIDUtil.newUuid()))
            .build();

            when(schedules.findById(Mockito.any())).thenReturn(Optional.of(whs));

            var wh = directory.readById(new FindByScheduleIdQuery(whs.getId().value()));

            assertNotNull(wh);
            assertEquals(whs.getId().value(), wh.get().id());
        
        Mockito.verify(schedules, Mockito.times(1)).findById(Mockito.any());
        Mockito.verifyNoMoreInteractions(schedules);
    }

    @Test
    void happyPath_readByParams_ShouldReturnNewObject() {

            when(schedules.findBySearchParams(Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(whSchedules());

            var wh = directory.readByParams(new FindByScheduleParamsQuery(null, "07:00", "16:00", "TUESDAY", null));

            assertNotNull(wh);
            assertEquals(5, wh.size(), "Size ashould be 5");
        
        Mockito.verify(schedules, Mockito.times(1)).findBySearchParams(Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(schedules);
    }

    @Test
    void unhappyPath_update_shouldThrowException() {

        Mockito.doNothing().when(schedules).update(Mockito.any());

        var ex = assertThrows(ValidationException.class, () -> directory.update(new UpdateWorkHourScheduleCmd(null, null, null, null, null)));

        assertEquals("Der opstod en fejl i opdateringen", ms.getMessage(Objects.requireNonNull(ex.messageKey()), null, DA));

        Mockito.verify(schedules, Mockito.times(0)).update(Mockito.any());
        Mockito.verifyNoMoreInteractions(schedules);


    }




    @Test
    void unhappyPath_create_shouldThrowValidationException() {
                
        when(schedules.save(Mockito.any())).thenThrow(new ValidationException("work_hour.create.failed", Map.of("create", "create failed")));


        var ex = assertThrows(ValidationException.class, () -> directory.create(new CreateWorkHourScheduleCmd("07:45", "15:45", "MONDAY", UUID.randomUUID())));
        assertEquals("Der opstod en fejl i oprettelsen", ms.getMessage(Objects.requireNonNull(ex.messageKey()), null, DA));

        Mockito.verify(schedules, Mockito.times(1)).save(Mockito.any());
        Mockito.verifyNoMoreInteractions(schedules);

    }

    @Test
    void unhappyPath_ReadById_WithNoKey_shouldThrowException() {

        WorkHourSchedule whs = WorkHourSchedule.builder()
            .id(WorkScheduleId.of(UUIDUtil.newUuid()))
            .startTime(WorkScheduleTime.of("07:00"))
            .endTime(WorkScheduleTime.of("16:00"))
            .weekDay(WeekDay.MONDAY)
            .sopRef(SopRef.of(UUIDUtil.newUuid()))
            .build();

        when(schedules.findById(Mockito.any())).thenReturn(Optional.of(whs));

        var ex = assertThrows(ValidationException.class, () -> directory.readById(new FindByScheduleIdQuery(null)));

        assertEquals("Key er krævet", ms.getMessage(Objects.requireNonNull(ex.messageKey()), null, DA));

        Mockito.verify(schedules, Mockito.times(0)).findById(Mockito.any());
        Mockito.verifyNoMoreInteractions(schedules);


    }


    private List<WorkHourSchedule> whSchedules() {
        return Arrays.asList(
            // 1. Mandag - normal arbejdsdag
            WorkHourSchedule.builder()
                .id(WorkScheduleId.of(UUIDUtil.newUuid()))
                .startTime(WorkScheduleTime.of("07:00"))
                .endTime(WorkScheduleTime.of("16:00"))
                .weekDay(WeekDay.MONDAY)
                .sopRef(SopRef.of(UUIDUtil.newUuid()))
                .build(),
                
            // 2. Tirsdag - senere start
            WorkHourSchedule.builder()
                .id(WorkScheduleId.of(UUIDUtil.newUuid()))
                .startTime(WorkScheduleTime.of("08:30"))
                .endTime(WorkScheduleTime.of("17:00"))
                .weekDay(WeekDay.TUESDAY)
                .sopRef(SopRef.of(UUIDUtil.newUuid()))
                .build(),
                
            // 3. Onsdag - kortere dag
            WorkHourSchedule.builder()
                .id(WorkScheduleId.of(UUIDUtil.newUuid()))
                .startTime(WorkScheduleTime.of("09:00"))
                .endTime(WorkScheduleTime.of("15:00"))
                .weekDay(WeekDay.WEDNESDAY)
                .sopRef(SopRef.of(UUIDUtil.newUuid()))
                .build(),
                
            // 4. Torsdag - lang dag
            WorkHourSchedule.builder()
                .id(WorkScheduleId.of(UUIDUtil.newUuid()))
                .startTime(WorkScheduleTime.of("06:00"))
                .endTime(WorkScheduleTime.of("18:00"))
                .weekDay(WeekDay.THURSDAY)
                .sopRef(SopRef.of(UUIDUtil.newUuid()))
                .build(),
                
            // 5. Fredag - tidlig afslutning
            WorkHourSchedule.builder()
                .id(WorkScheduleId.of(UUIDUtil.newUuid()))
                .startTime(WorkScheduleTime.of("07:30"))
                .endTime(WorkScheduleTime.of("14:30"))
                .weekDay(WeekDay.FRIDAY)
                .sopRef(SopRef.of(UUIDUtil.newUuid()))
                .build()
        );
    } 


}
