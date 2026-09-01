package local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import local.sop.datawarehouse.sharedlib.enums.ConsentPurpose;
import local.sop.datawarehouse.sharedlib.enums.ConsentType;



public interface ConsentStatementSpringDataRepository extends JpaRepository<ConsentStatementEntity, UUID> {
	
    Optional<ConsentStatementEntity> findById(UUID id);
    List<ConsentStatementEntity> findAll();
	
    @Modifying(clearAutomatically = true) // Clear the persistence context after the update to ensure we get the updated entity in subsequent queries
    @Query("UPDATE ConsentStatementEntity c SET c.active = :active WHERE c.id = :id")
    int updateActiveStatus(@Param("id") UUID id, @Param("active") Boolean active);
    
    // CHANGED: purpose/type parameters added, both optional (COALESCE
    // keeps the existing value when null is passed) — matches how
    // active/statementText already work via updateStatement().
    @Modifying(clearAutomatically = true) // Clear the persistence context after the update to ensure we get the updated entity in subsequent queries
    @Query("""
        UPDATE ConsentStatementEntity c SET
            c.active = :active,
            c.statementText = :statementText,
            c.purpose = COALESCE(:purpose, c.purpose),
            c.type = COALESCE(:type, c.type)
        WHERE c.id = :id
        """)
    int updateStatement(@Param("id") UUID id, @Param("active") Boolean active, @Param("statementText") String statementText,
            @Param("purpose") ConsentPurpose purpose, @Param("type") ConsentType type);
 
    @Modifying(clearAutomatically = true)
	@Query("DELETE ConsentStatementEntity c WHERE c.id = id")
    int delete(@Param("id") UUID id);
}