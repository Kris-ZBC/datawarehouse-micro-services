package local.sop.datawarehouse.registration.saga.application.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)

public class PropsTest {

    @Nested
    class ApprenticePropsTest {

        @Test
        void recordStoresBaseUrl() {
            ApprenticeProps props = new ApprenticeProps("A");
            assertEquals("A", props.baseUrl());
            assertEquals("ApprenticeProps[baseUrl=A]", props.toString());
        }
    }

    @Nested
    class AuditlogPropsTest {

        @Test
        void recordStoresBaseUrl() {
            AuditlogProps props = new AuditlogProps("B");
            assertEquals("B", props.baseurl());
            assertEquals("AuditlogProps[baseurl=B]", props.toString());
        }
    }

    @Nested
    class ConsentPropsTest {

        @Test
        void recordStoresBaseUrl() {
            ConsentProps props = new ConsentProps("C");
            assertEquals("C", props.baseUrl());
            assertEquals("ConsentProps[baseUrl=C]", props.toString());
        }
    }

    @Nested
    class ConsentSagaPropsTest {

        @Test
        void recordStoresBaseUrl() {
            ConsentSagaProps props = new ConsentSagaProps("CS");
            assertEquals("CS", props.baseUrl());
            assertEquals("ConsentSagaProps[baseUrl=CS]", props.toString());
        }
    }



    @Nested
    class EducationLinePropsTest {

        @Test
        void recordStoresBaseUrl() {
            EducationLineProps props = new EducationLineProps("D");
            assertEquals("D", props.baseUrl());
            assertEquals("EducationLineProps[baseUrl=D]", props.toString());
        }
    }

    @Nested
    class InstructorPropsTest {

        @Test
        void recordStoresBaseUrl() {
            InstructorProps props = new InstructorProps("E");
            assertEquals("E", props.baseUrl());
            assertEquals("InstructorProps[baseUrl=E]", props.toString());
        }
    }
    @Nested
    class LoginPropsTest {

        @Test
        void recordStoresBaseUrl() {
            LoginProps props = new LoginProps("F");
            assertEquals("F", props.baseUrl());
            assertEquals("LoginProps[baseUrl=F]", props.toString());
        }
    }
    @Nested
    class OrganizationPropsTest {

        @Test
        void recordStoresBaseUrl() {
            OrganizationProps props = new OrganizationProps("G");
            assertEquals("G", props.baseUrl());
            assertEquals("OrganizationProps[baseUrl=G]", props.toString());
        }
    }
    @Nested
    class PersonPropsTest {

        @Test
        void recordStoresBaseUrl() {
            PersonProps props = new PersonProps("H");
            assertEquals("H", props.baseUrl());
            assertEquals("PersonProps[baseUrl=H]", props.toString());
        }
    }
}
