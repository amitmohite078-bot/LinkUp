package com.linkup.service;

import com.linkup.dto.*;
import com.linkup.model.Block;
import com.linkup.model.User;
import com.linkup.repository.BlockRepository;
import com.linkup.repository.FriendshipRepository;
import com.linkup.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    public User register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .passwordHash(PasswordUtil.hashPassword(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .privacyPosts("PUBLIC")
                .privacyRequests("ALL")
                .privacyFriendsList("PUBLIC")
                .privacySearch("PUBLIC")
                .privacyMessage("ALL")
                .privacyProfile("PUBLIC")
                .build();

        return userRepository.save(user);
    }

    public User login(LoginRequest req) {
        Optional<User> userOpt = req.getEmailOrUsername().contains("@")
                ? userRepository.findByEmail(req.getEmailOrUsername())
                : userRepository.findByUsername(req.getEmailOrUsername());

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid username/email or password");
        }

        User user = userOpt.get();
        if (!PasswordUtil.checkPassword(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid username/email or password");
        }

        return user;
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> searchUsers(String query) {
        return userRepository.searchUsers(query);
    }

    public List<User> getFriendSuggestions(Long userId) {
        return userRepository.getFriendSuggestions(userId);
    }

    @Transactional
    public User updateProfile(Long userId, String firstName, String lastName, String bio, 
                              String relationshipStatus, String work, String education, String location,
                              String avatarUrl, String coverUrl) {
        User user = getById(userId);
        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (bio != null) user.setBio(bio);
        if (relationshipStatus != null) user.setRelationshipStatus(relationshipStatus);
        if (work != null) user.setWork(work);
        if (education != null) user.setEducation(education);
        if (location != null) user.setLocation(location);
        if (avatarUrl != null) user.setAvatarUrl(avatarUrl);
        if (coverUrl != null) user.setCoverUrl(coverUrl);

        return userRepository.save(user);
    }

    @Transactional
    public User updatePrivacy(Long userId, String privacyPosts, String privacyRequests, 
                              String privacyFriendsList, String privacySearch, String privacyMessage, 
                              String privacyProfile) {
        User user = getById(userId);
        if (privacyPosts != null) user.setPrivacyPosts(privacyPosts);
        if (privacyRequests != null) user.setPrivacyRequests(privacyRequests);
        if (privacyFriendsList != null) user.setPrivacyFriendsList(privacyFriendsList);
        if (privacySearch != null) user.setPrivacySearch(privacySearch);
        if (privacyMessage != null) user.setPrivacyMessage(privacyMessage);
        if (privacyProfile != null) user.setPrivacyProfile(privacyProfile);

        return userRepository.save(user);
    }

    @Transactional
    public void blockUser(Long blockerId, Long blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new RuntimeException("You cannot block yourself");
        }

        if (blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            return; // Already blocked
        }

        User blocker = getById(blockerId);
        User blocked = getById(blockedId);

        Block block = Block.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build();
        blockRepository.save(block);

        // Blocking removes any existing friendship/requests between the two users
        friendshipRepository.findFriendshipBetween(blockerId, blockedId).ifPresent(f -> {
            friendshipRepository.delete(f);
        });
    }

    @Transactional
    public void unblockUser(Long blockerId, Long blockedId) {
        blockRepository.findByBlockerIdAndBlockedId(blockerId, blockedId).ifPresent(block -> {
            blockRepository.delete(block);
        });
    }

    public boolean isBlocked(Long userAId, Long userBId) {
        return blockRepository.isBlockedEitherWay(userAId, userBId);
    }
}
