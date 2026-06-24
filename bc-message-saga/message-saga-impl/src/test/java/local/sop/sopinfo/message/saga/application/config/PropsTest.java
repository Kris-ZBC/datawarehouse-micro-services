package local.sop.sopinfo.message.saga.application.config;

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
			AuditLogProps props = new AuditLogProps("B");
			assertEquals("B", props.baseUrl());
			assertEquals("AuditLogProps[baseUrl=B]", props.toString());
		}
	}

	@Nested
	class EducationInstructorPropsTest {
		@Test
		void recordStoresBaseUrl() {
			EducationInstructorProps props = new EducationInstructorProps("C");
			assertEquals("C", props.baseUrl());
			assertEquals("EducationInstructorProps[baseUrl=C]", props.toString());
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
	class MessagePersonPropsTest {
		@Test
		void recordStoresBaseUrl() {
			MessagePersonProps props = new MessagePersonProps("F");
			assertEquals("F", props.baseUrl());
			assertEquals("MessagePersonProps[baseUrl=F]", props.toString());
		}
	}

	@Nested
	class MessagePropsTest {
		@Test
		void recordStoresBaseUrl() {
			MessageProps props = new MessageProps("G");
			assertEquals("G", props.baseUrl());
			assertEquals("MessageProps[baseUrl=G]", props.toString());
		}
	}

	@Nested
	class NotificationPropsTest {
		@Test
		void recordStoresBaseUrl() {
			NotificationProps props = new NotificationProps("H");
			assertEquals("H", props.baseUrl());
			assertEquals("NotificationProps[baseUrl=H]", props.toString());
		}
	}

	@Nested
	class PersonNotificationPropsTest {
		@Test
		void recordStoresBaseUrl() {
			PersonNotificationProps props = new PersonNotificationProps("I");
			assertEquals("I", props.baseUrl());
			assertEquals("PersonNotificationProps[baseUrl=I]", props.toString());
		}
	}

	@Nested
	class PersonPropsTest {
		@Test
		void recordStoresBaseUrl() {
			PersonProps props = new PersonProps("J");
			assertEquals("J", props.baseUrl());
			assertEquals("PersonProps[baseUrl=J]", props.toString());
		}
	}
}
