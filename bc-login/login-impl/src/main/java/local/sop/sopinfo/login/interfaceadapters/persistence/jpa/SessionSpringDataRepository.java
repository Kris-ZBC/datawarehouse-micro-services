package local.sop.sopinfo.login.interfaceadapters.persistence.jpa;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionSpringDataRepository extends JpaRepository<SessionEntity, UUID>{
	
	Optional<SessionEntity> findBySessionToken(String sessionToken);

    // Find the most recent non-expired session for this login
   @Query("SELECT s FROM SessionEntity s WHERE s.login.id = :loginId " +
       "AND s.expiresAt > :now ORDER BY s.createdAt DESC")
	Optional<SessionEntity> findActiveByLoginId(
		@Param("loginId") UUID loginId,
		@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM SessionEntity s WHERE s.login.id = :loginId")
    void deleteByLoginId(@Param("loginId") UUID loginId);

	@Modifying
	@Query("DELETE FROM SessionEntity s WHERE s.sessionToken = :token")
	int deleteBySessionToken(@Param("token") String token);
}
