package local.sop.datawarehouse.login.saga.application.infrastructure.instructor;

import java.util.UUID;
 
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
 
import local.sop.datawarehouse.login.saga.application.ports.out.instructor.InstructorPort;
 
@Component
public class InstructorHttpAdapter implements InstructorPort {
 
	private final RestClient instructor;
 
	public InstructorHttpAdapter(@Qualifier("instructor") RestClient instructor) {
		this.instructor = instructor;
	}
 
	// Existence-only check — bc-instructor's endpoint returns 200 with
	// the InstructorResponse body if the person IS an instructor, 204
	// No Content if not. Checking the status code directly avoids
	// needing to mirror InstructorResponse's shape locally at all,
	// since the saga only cares about the yes/no answer.
	@Override
	public boolean isInstructor(UUID personRef) {
		var response = instructor.get()
			.uri("/internal/instructors/by-person-ref/{personRef}", personRef)
			.retrieve()
			.toBodilessEntity();
		return response.getStatusCode().value() == 200;
	}
}
