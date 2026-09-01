package com.linkup.ranking;

import com.linkup.dto.PostResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("chronological")
public class ChronologicalRankingStrategy implements FeedRankingStrategy {
    @Override
    public List<PostResponse> rank(List<PostResponse> posts, Long currentUserId) {
        List<PostResponse> sorted = new ArrayList<>(posts);
        sorted.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
        return sorted;
    }
}
