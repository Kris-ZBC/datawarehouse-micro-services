package local.sop.datawarehouse.instructor.interfaceadapters.persistence.jpa;

import org.springframework.stereotype.Component;

import local.sop.datawarehouse.instructor.domain.ports.out.InstructorRepositoryPort;
import local.sop.datawarehouse.sharedlib.login.CheckIntructorExist;




@Component
public class InstructorExistAdapter implements CheckIntructorExist {

	private final InstructorRepositoryPort instructorRepositoryPort;

	public InstructorExistAdapter(InstructorRepositoryPort instructorRepositoryPort) {
		this.instructorRepositoryPort = instructorRepositoryPort;
	}

	@Override
	public boolean instructorExist() {
		return instructorRepositoryPort.exists();
	}

}