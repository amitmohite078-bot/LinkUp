package com.linkup.ranking;

import com.linkup.dto.PostResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("engagement")
public class EngagementRankingStrategy implements FeedRankingStrategy {
    @Override
    public List<PostResponse> rank(List<PostResponse> posts, Long currentUserId) {
        List<PostResponse> sorted = new ArrayList<>(posts);
        sorted.sort((p1, p2) -> Double.compare(calculateScore(p2), calculateScore(p1)));
        return sorted;
    }

    private double calculateScore(PostResponse post) {
        long reactions = 0;
        if (post.getReactionCounts() != null) {
            reactions = post.getReactionCounts().values().stream().mapToLong(Long::longValue).sum();
        }
        long comments = post.getCommentCount();
        long shares = post.getShareCount();

        // Engagement Formula: (Reactions * 1) + (Comments * 3) + (Shares * 5)
        return (reactions * 1.0) + (comments * 3.0) + (shares * 5.0);
    }
}
