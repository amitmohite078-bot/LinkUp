package com.linkup.repository;

import com.linkup.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupId(Long groupId);
    List<GroupMember> findByGroupIdAndStatus(Long groupId, String status);
    List<GroupMember> findByUserIdAndStatus(Long userId, String status);
    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);
    boolean existsByGroupIdAndUserIdAndStatus(Long groupId, Long userId, String status);
}
