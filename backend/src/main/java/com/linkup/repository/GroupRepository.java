package com.linkup.repository;

import com.linkup.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByCreatorId(Long creatorId);

    @Query("SELECT g FROM Group g WHERE " +
           "(LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(g.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "g.privacy <> 'HIDDEN'")
    List<Group> searchGroups(@Param("query") String query);

    // Group recommendations: groups which are not hidden and where user is not active/pending member
    @Query("SELECT g FROM Group g WHERE g.privacy <> 'HIDDEN' AND g.id NOT IN (" +
           "SELECT gm.group.id FROM GroupMember gm WHERE gm.user.id = :userId" +
           ")")
    List<Group> getGroupRecommendations(@Param("userId") Long userId);
}
