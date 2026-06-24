package local.sop.sopinfo.consent.interfaceadapters.persistence.jpa.consentstatement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



public interface ConsentStatementSpringDataRepository extends JpaRepository<ConsentStatementEntity, UUID> {
	
	Optional<ConsentStatementEntity> findById(UUID id);
    List<ConsentStatementEntity> findAll();
	
    @Modifying(clearAutomatically = true) // Clear the persistence context after the update to ensure we get the updated entity in subsequent queries
    @Query("UPDATE ConsentStatementEntity c SET c.active = :active WHERE c.id = :id")
    int updateActiveStatus(@Param("id") UUID id, @Param("active") Boolean active);
    
    @Modifying(clearAutomatically = true) // Clear the persistence context after the update to ensure we get the updated entity in subsequent queries
    @Query("UPDATE ConsentStatementEntity c SET c.active = :active, c.statementText = :statementText WHERE c.id = :id")
    int updateStatement(@Param("id") UUID id, @Param("active") Boolean active, @Param("statementText") String statementText);

    @Modifying(clearAutomatically = true)
	@Query("DELETE ConsentStatementEntity c WHERE c.id = id")
    int delete(@Param("id") UUID id);
}