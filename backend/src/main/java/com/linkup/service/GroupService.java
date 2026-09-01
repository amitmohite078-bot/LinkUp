package com.linkup.service;

import com.linkup.model.*;
import com.linkup.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private GroupRuleRepository groupRuleRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public Group createGroup(Long creatorId, String name, String description, String privacy, List<String> rules) {
        User creator = userService.getById(creatorId);

        Group group = Group.builder()
                .name(name)
                .description(description)
                .privacy(privacy != null ? privacy : "PUBLIC")
                .creator(creator)
                .build();

        Group saved = groupRepository.save(group);

        // Auto-add creator as ADMIN and ACTIVE member
        GroupMember member = GroupMember.builder()
                .group(saved)
                .user(creator)
                .role("ADMIN")
                .status("ACTIVE")
                .build();
        groupMemberRepository.save(member);

        // Add rules if any
        if (rules != null) {
            List<GroupRule> groupRules = rules.stream()
                    .map(r -> GroupRule.builder().group(saved).ruleText(r).build())
                    .collect(Collectors.toList());
            groupRuleRepository.saveAll(groupRules);
        }

        return saved;
    }

    @Transactional
    public GroupMember joinGroup(Long userId, Long groupId) {
        User user = userService.getById(userId);
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        Optional<GroupMember> existing = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
        if (existing.isPresent()) {
            return existing.get(); // Already joined or pending
        }

        // Private/Hidden groups require admin approval, public groups join instantly
        String status = group.getPrivacy().equals("PUBLIC") ? "ACTIVE" : "PENDING";

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .role("MEMBER")
                .status(status)
                .build();

        GroupMember saved = groupMemberRepository.save(member);

        // If pending, notify group creator/admin
        if (status.equals("PENDING")) {
            String content = user.getFirstName() + " " + user.getLastName() + " requested to join group " + group.getName() + ".";
            notificationService.createNotification(group.getCreator(), user, "GROUP_JOIN_REQUEST", group.getId(), content);
        }

        return saved;
    }

    @Transactional
    public void leaveGroup(Long userId, Long groupId) {
        groupMemberRepository.findByGroupIdAndUserId(groupId, userId).ifPresent(gm -> {
            if (gm.getRole().equals("ADMIN")) {
                // Check if they are the only admin
                List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
                boolean otherAdminExists = members.stream()
                        .anyMatch(m -> m.getRole().equals("ADMIN") && !m.getUser().getId().equals(userId));
                if (!otherAdminExists && members.size() > 1) {
                    throw new RuntimeException("Cannot leave: Assign another group administrator first");
                }
            }
            groupMemberRepository.delete(gm);
        });
    }

    @Transactional
    public void approveMember(Long adminId, Long groupId, Long targetUserId) {
        validateAdminOrModerator(adminId, groupId);

        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new RuntimeException("Membership request not found"));

        member.setStatus("ACTIVE");
        groupMemberRepository.save(member);

        // Notify member
        String content = "Your request to join group " + member.getGroup().getName() + " was approved.";
        notificationService.createNotification(member.getUser(), null, "GROUP_JOIN_APPROVED", groupId, content);
    }

    @Transactional
    public void rejectMember(Long adminId, Long groupId, Long targetUserId) {
        validateAdminOrModerator(adminId, groupId);

        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new RuntimeException("Membership request not found"));

        groupMemberRepository.delete(member);
    }

    @Transactional
    public void changeMemberRole(Long adminId, Long groupId, Long targetUserId, String newRole) {
        // Only administrators can assign roles
        GroupMember admin = groupMemberRepository.findByGroupIdAndUserId(groupId, adminId)
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));

        if (!admin.getRole().equals("ADMIN")) {
            throw new RuntimeException("Unauthorized: Only administrators can assign moderator/admin permissions");
        }

        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new RuntimeException("Target member not found"));

        member.setRole(newRole.toUpperCase());
        groupMemberRepository.save(member);
    }

    public List<Group> getRecommendedGroups(Long userId) {
        return groupRepository.getGroupRecommendations(userId);
    }

    public List<Group> searchGroups(String query) {
        return groupRepository.searchGroups(query);
    }

    public Group getById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
    }

    public List<GroupMember> getGroupMembers(Long groupId) {
        return groupMemberRepository.findByGroupId(groupId);
    }

    public List<GroupRule> getGroupRules(Long groupId) {
        return groupRuleRepository.findByGroupId(groupId);
    }

    public boolean isUserActiveMember(Long userId, Long groupId) {
        return groupMemberRepository.existsByGroupIdAndUserIdAndStatus(groupId, userId, "ACTIVE");
    }

    private void validateAdminOrModerator(Long userId, Long groupId) {
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new RuntimeException("Access denied: You are not a member of this group"));

        if (!member.getRole().equals("ADMIN") && !member.getRole().equals("MODERATOR")) {
            throw new RuntimeException("Access denied: Administrator or Moderator role required");
        }
    }
}
