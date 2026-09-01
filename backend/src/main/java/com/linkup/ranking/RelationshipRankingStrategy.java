package com.linkup.ranking;

import com.linkup.dto.PostResponse;
import com.linkup.repository.FriendshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component("relationship")
public class RelationshipRankingStrategy implements FeedRankingStrategy {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Override
    public List<PostResponse> rank(List<PostResponse> posts, Long currentUserId) {
        if (currentUserId == null) {
            return posts;
        }

        // Fetch active friend IDs
        Set<Long> friendIds = friendshipRepository.findActiveFriendships(currentUserId).stream()
                .map(f -> f.getRequester().getId().equals(currentUserId) ? f.getAddressee().getId() : f.getRequester().getId())
                .collect(Collectors.toSet());

        List<PostResponse> sorted = new ArrayList<>(posts);
        sorted.sort((p1, p2) -> {
            boolean isFriend1 = friendIds.contains(p1.getUserId()) || p1.getUserId().equals(currentUserId);
            boolean isFriend2 = friendIds.contains(p2.getUserId()) || p2.getUserId().equals(currentUserId);

            if (isFriend1 && !isFriend2) return -1;
            if (!isFriend1 && isFriend2) return 1;

            // Tie breaker: recency
            return p2.getCreatedAt().compareTo(p1.getCreatedAt());
        });

        return sorted;
    }
}
