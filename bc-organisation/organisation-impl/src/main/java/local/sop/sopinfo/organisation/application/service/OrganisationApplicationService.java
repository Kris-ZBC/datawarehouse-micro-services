package local.sop.sopinfo.organisation.application.service;

import java.util.Optional;
import java.util.UUID;
import java.util.Map;

import local.sop.sopinfo.organisation.application.api.OrganisationDirectory;
import local.sop.sopinfo.organisation.application.api.dto.OrganisationQuery;
import local.sop.sopinfo.organisation.application.api.dto.OrganisationResponse;
import local.sop.sopinfo.organisation.interfaceadapters.persistence.jpa.OrganisationSpringDataRepository;

// Exception
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganisationApplicationService implements OrganisationDirectory {
	OrganisationSpringDataRepository organisations;
	private static final Logger log = LoggerFactory.getLogger(OrganisationApplicationService.class);

	public OrganisationApplicationService(OrganisationSpringDataRepository repo) {
		this.organisations = repo;
	}

	@Transactional(readOnly = true)
	@Override
	public Optional<OrganisationResponse> findById(UUID id) {
		try {
			log.info("Finding organisation with id: {}", id.toString());
			var query = new OrganisationQuery(id);
			var organisation = this.organisations.findById(query.id());
			if (organisation.isEmpty()) {
				throw new NotFoundException("organisation.notFound", Map.of("id", id));
			}
			var response = organisation.map(p -> new OrganisationResponse(p.getId(), p.getName(), p.getCvr()));
			return response;
		}
		catch (NotFoundException e) {
			log.warn("Organisation with id: {} not found", id.toString());
			throw e;
		}
		catch (Exception e) {
			log.error("Error finding organisation with id: {}", id.toString(), e);
			throw new ValidationException("organisation.failed.findById", Map.of("id", id));
		}
	}
}
