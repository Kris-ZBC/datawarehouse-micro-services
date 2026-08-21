package local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentStatus;
import local.sop.common.libs.sharedkernel.enums.ConsentType;

public interface ConsentSpringDataRepository extends JpaRepository<ConsentEntity, UUID> {



	Optional<ConsentEntity> findById(UUID id);
 
	@Query("SELECT c FROM ConsentEntity c WHERE c.personReference = :personReference AND c.consentStatement.id = :statementReference")
    Optional<ConsentEntity> findByPersonAndStatementReference(@Param("personReference") UUID personReference, @Param("statementReference") UUID statementReference);
 
	@Query("SELECT c FROM ConsentEntity c")
	List<ConsentEntity> findAll();
 
	@Query("SELECT c FROM ConsentEntity c WHERE c.personReference = :personReference")
	List<ConsentEntity> findByPersonReference(@Param("personReference") UUID personReference);
 
 
	// CHANGED: purpose/type moved to ConsentStatementEntity, so these
	// filters now join through c.consentStatement instead of reading
	// c.purpose/c.type directly. status stays a direct filter on
	// ConsentEntity since it's still a Consent-level property.
	@Query("""
		SELECT c FROM ConsentEntity c JOIN c.consentStatement cs WHERE
		(:status IS NULL OR c.status = :status) AND
		(:purpose IS NULL OR cs.purpose = :purpose) AND
		(:type IS NULL OR cs.type = :type)
		""")
	List<ConsentEntity> findByStatusAndPurposeAndType(
			@Param("status") ConsentStatus status,
			@Param("purpose") ConsentPurpose purpose,
			@Param("type") ConsentType type);
 
	@Query("SELECT c FROM ConsentEntity c JOIN c.consentStatement cs WHERE c.personReference = :personReference AND cs.purpose = :purpose")
	Optional<ConsentEntity> findByPersonAndPurpose(@Param("personReference") UUID personReference, @Param("purpose") ConsentPurpose purpose);
 
	@Modifying(clearAutomatically = true) // Clear the persistence context after the update to ensure we get the updated entity in subsequent queries
	@Query("UPDATE ConsentEntity c SET c.status = COALESCE(:status, c.status) WHERE c.id = :id")
	void updateWithdrawnStatus(@Param("status") ConsentStatus status, @Param("id") UUID id);
 
    @Modifying(clearAutomatically = true)
	@Query("DELETE ConsentEntity c WHERE c.id = id")
    int delete(@Param("id") UUID id);

}