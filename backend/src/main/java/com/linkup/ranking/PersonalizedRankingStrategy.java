package com.linkup.ranking;

import com.linkup.dto.PostResponse;
import com.linkup.repository.FriendshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component("personalized")
public class PersonalizedRankingStrategy implements FeedRankingStrategy {

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

        LocalDateTime now = LocalDateTime.now();

        List<PostResponse> sorted = new ArrayList<>(posts);
        sorted.sort((p1, p2) -> Double.compare(
                calculatePersonalizedScore(p2, currentUserId, friendIds, now),
                calculatePersonalizedScore(p1, currentUserId, friendIds, now)
        ));

        return sorted;
    }

    private double calculatePersonalizedScore(PostResponse post, Long currentUserId, Set<Long> friendIds, LocalDateTime now) {
        double score = 0.0;

        // 1. Relationship Boost
        if (post.getUserId().equals(currentUserId) || friendIds.contains(post.getUserId())) {
            score += 100.0; // Significant boost for friends/self
        }

        // 2. Engagement Weight
        long reactions = 0;
        if (post.getReactionCounts() != null) {
            reactions = post.getReactionCounts().values().stream().mapToLong(Long::longValue).sum();
        }
        score += (reactions * 1.0) + (post.getCommentCount() * 3.0) + (post.getShareCount() * 5.0);

        // 3. Recency Time Decay (linear penalty per hour)
        long hoursOld = Duration.between(post.getCreatedAt(), now).toHours();
        double timeDecay = hoursOld * 0.5; // lose 0.5 points per hour
        score = Math.max(0.0, score - timeDecay);

        return score;
    }
}
