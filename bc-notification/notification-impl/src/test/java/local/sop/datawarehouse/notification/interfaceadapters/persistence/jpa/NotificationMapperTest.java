package local.sop.datawarehouse.notification.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

import java.util.UUID;

// import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import local.sop.datawarehouse.notification.domain.model.Notification;
import local.sop.datawarehouse.notification.domain.model.valueobjects.MessageRef;
import local.sop.datawarehouse.notification.domain.model.valueobjects.NotificationId;

public class NotificationMapperTest {
	
	 
	 private final NotificationMapper mapper = new NotificationMapper();	

	@Test
	@DisplayName("should map entity to domain notification")
	void shouldMapEntityToDomain(){
		UUID notificationId = UUID.randomUUID();
		UUID messageRef = UUID.randomUUID();

		NotificationEntity entity = NotificationEntity.builder()
				.id(notificationId)
				.messageRef(messageRef)
				.build();

		Notification result = mapper.toDomain(entity);
			assertNotNull(result);
			assertEquals(notificationId,result.getId().value());
			assertEquals(messageRef,result.getMessageRef().value());
			assertFalse(result.getSeen());
	}

	@Test
	@DisplayName("should map domain notification to entity")
	void shouldMapDomainToEntity(){
		UUID notificationId = UUID.randomUUID();
		UUID messageRef = UUID.randomUUID();

		Notification notification = Notification.builder()
				.id(new NotificationId(notificationId))
				.messageRef(new MessageRef(messageRef))
				.build();

		NotificationEntity result = mapper.toEntity(notification);
			assertNotNull(result);
			assertEquals(notificationId,result.getId());
			assertEquals(messageRef,result.getMessageRef());		


	}


}
