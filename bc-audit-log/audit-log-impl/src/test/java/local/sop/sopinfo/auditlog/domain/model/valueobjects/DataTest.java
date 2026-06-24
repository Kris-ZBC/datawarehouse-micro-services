package local.sop.sopinfo.auditlog.domain.model.valueobjects;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataTest {

    // --------------------
    // Constructor tests
    // --------------------

    @Test
    void constructor_shouldThrow_whenNull() {
        assertThrows(ValidationException.class, () -> {
            new Data(null);
        });
    }

    @Test
    void constructor_shouldThrow_whenBlank() {
        assertThrows(ValidationException.class, () -> {
            new Data("   ");
        });
    }

    @Test
    void constructor_shouldKeepValue_whenValid() {
        Data data = new Data("{\"key\":\"value\"}");

        assertEquals("{\"key\":\"value\"}", data.json());
    }

    // --------------------
    // Factory method tests
    // --------------------

    @Test
    void newData_shouldThrow_whenNull() {
        assertThrows(ValidationException.class, () -> {
            Data.newData(null);
        });
    }

    @Test
    void newData_shouldThrow_whenBlank() {
        assertThrows(ValidationException.class, () -> {
            Data.newData("   ");
        });
    }

    @Test
    void newData_shouldCreate_whenValid() {
        Data data = Data.newData("{\"test\":true}");

        assertEquals("{\"test\":true}", data.json());
    }

    // --------------------
    // toString test
    // --------------------

    @Test
    void toString_shouldReturnJson() {
        Data data = new Data("{\"a\":1}");

        assertEquals("{\"a\":1}", data.toString());
    }
}