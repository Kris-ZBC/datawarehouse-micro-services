package local.sop.datawarehouse.instructor.interfaceadapters.persistence.jpa;

import org.springframework.stereotype.Component;

import local.sop.common.libs.sharedkernel.login.CheckIntructorExist;
import local.sop.datawarehouse.instructor.domain.ports.out.InstructorRepositoryPort;




@Component
public class InstructorExistAdapter implements CheckIntructorExist {

	private final InstructorRepositoryPort instructorRepositoryPort;

	public InstructorExistAdapter(InstructorRepositoryPort instructorRepositoryPort) {
		this.instructorRepositoryPort = instructorRepositoryPort;
	}

	@Override
	public boolean instructorExist() {
		return !instructorRepositoryPort.findAll().isEmpty();
	}

}