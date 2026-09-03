package local.sop.datawarehouse.login.saga.application.infrastructure.apprentice;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
 
import local.sop.datawarehouse.login.saga.application.ports.out.apprentice.ApprenticePort;
 
@Component
public class ApprenticeHttpAdapter implements ApprenticePort {
 
	private final RestClient apprentice;
 
	public ApprenticeHttpAdapter(@Qualifier("apprentice") RestClient apprentice) {
		this.apprentice = apprentice;
	}
 
	@Override
	public boolean isApprentice(UUID personRef) {
		var response = apprentice.get()
			.uri("/internal/apprentices/by-person-ref/{personRef}", personRef)
			.retrieve()
			.toBodilessEntity();
		return response.getStatusCode().value() == 200;
	}
}
