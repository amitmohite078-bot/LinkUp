package com.linkup.repository;

import com.linkup.model.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {
    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
    Optional<Block> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
    List<Block> findByBlockerId(Long blockerId);

    // Get block status in either direction
    @Query("SELECT COUNT(b) > 0 FROM Block b WHERE " +
           "(b.blocker.id = :userAId AND b.blocked.id = :userBId) OR " +
           "(b.blocker.id = :userBId AND b.blocked.id = :userAId)")
    boolean isBlockedEitherWay(@Param("userAId") Long userAId, @Param("userBId") Long userBId);
}
