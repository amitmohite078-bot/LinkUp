package com.linkup.service;

import com.linkup.model.Friendship;
import com.linkup.model.User;
import com.linkup.repository.FriendshipRepository;
import com.linkup.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FriendshipService {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public Friendship sendFriendRequest(Long requesterId, Long addresseeId) {
        if (requesterId.equals(addresseeId)) {
            throw new RuntimeException("You cannot send a friend request to yourself");
        }

        if (userService.isBlocked(requesterId, addresseeId)) {
            throw new RuntimeException("Action restricted: User is blocked");
        }

        Optional<Friendship> existing = friendshipRepository.findFriendshipBetween(requesterId, addresseeId);
        if (existing.isPresent()) {
            throw new RuntimeException("Friendship or request already exists");
        }

        User requester = userService.getById(requesterId);
        User addressee = userService.getById(addresseeId);

        Friendship friendship = Friendship.builder()
                .requester(requester)
                .addressee(addressee)
                .status("PENDING")
                .build();

        Friendship saved = friendshipRepository.save(friendship);

        // Notify addressee
        String content = requester.getFirstName() + " " + requester.getLastName() + " sent you a friend request.";
        notificationService.createNotification(addressee, requester, "FRIEND_REQUEST", saved.getId(), content);

        return saved;
    }

    @Transactional
    public Friendship acceptFriendRequest(Long userId, Long requesterId) {
        Friendship friendship = friendshipRepository.findFriendshipBetween(userId, requesterId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        if (!friendship.getAddressee().getId().equals(userId)) {
            throw new RuntimeException("You cannot accept this friend request");
        }

        friendship.setStatus("ACCEPTED");
        Friendship saved = friendshipRepository.save(friendship);

        User accepter = friendship.getAddressee();
        User requester = friendship.getRequester();

        // Notify requester
        String content = accepter.getFirstName() + " " + accepter.getLastName() + " accepted your friend request.";
        notificationService.createNotification(requester, accepter, "FRIEND_REQUEST_ACCEPT", saved.getId(), content);

        return saved;
    }

    @Transactional
    public void deleteFriendRequest(Long userId, Long requesterId) {
        Friendship friendship = friendshipRepository.findFriendshipBetween(userId, requesterId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        if (!friendship.getAddressee().getId().equals(userId) && !friendship.getRequester().getId().equals(userId)) {
            throw new RuntimeException("You cannot delete this request");
        }

        friendshipRepository.delete(friendship);
    }

    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        Friendship friendship = friendshipRepository.findFriendshipBetween(userId, friendId)
                .orElseThrow(() -> new RuntimeException("Friendship not found"));

        friendshipRepository.delete(friendship);
    }

    public List<User> getFriendsList(Long userId) {
        List<Friendship> friendships = friendshipRepository.findActiveFriendships(userId);
        return friendships.stream()
                .map(f -> f.getRequester().getId().equals(userId) ? f.getAddressee() : f.getRequester())
                .collect(Collectors.toList());
    }

    public List<Friendship> getPendingRequests(Long userId) {
        return friendshipRepository.findByAddresseeIdAndStatus(userId, "PENDING");
    }

    public List<Friendship> getSentRequests(Long userId) {
        return friendshipRepository.findByRequesterIdAndStatus(userId, "PENDING");
    }

    public List<User> getMutualFriends(Long userAId, Long userBId) {
        List<User> friendsA = getFriendsList(userAId);
        List<User> friendsB = getFriendsList(userBId);

        List<User> mutual = new ArrayList<>(friendsA);
        mutual.retainAll(friendsB);
        return mutual;
    }

    public String getFriendshipStatus(Long userAId, Long userBId) {
        if (userAId.equals(userBId)) {
            return "SELF";
        }
        Optional<Friendship> friendshipOpt = friendshipRepository.findFriendshipBetween(userAId, userBId);
        if (friendshipOpt.isEmpty()) {
            return "NONE";
        }
        Friendship f = friendshipOpt.get();
        if (f.getStatus().equals("ACCEPTED")) {
            return "FRIENDS";
        }
        if (f.getRequester().getId().equals(userAId)) {
            return "REQUEST_SENT";
        }
        return "REQUEST_RECEIVED";
    }
}
