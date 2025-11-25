package com.user.UserService.user.repository;

import com.user.UserService.user.domain.entity.DeviceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceSessionRepository extends JpaRepository<DeviceSession, UUID> {

    @Query("SELECT ds FROM DeviceSession ds WHERE ds.userId = :userId AND ds.revoked = false AND ds.deletedAt IS NULL ORDER BY ds.lastUsedAt DESC")
    List<DeviceSession> findActiveByUserId(@Param("userId") UUID userId);

    @Query("SELECT ds FROM DeviceSession ds WHERE ds.id = :id AND ds.userId = :userId AND ds.deletedAt IS NULL")
    Optional<DeviceSession> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE DeviceSession ds SET ds.revoked = true WHERE ds.userId = :userId")
    void revokeAllByUserId(@Param("userId") UUID userId);

    List<DeviceSession> findByUserId(UUID userId);
}
