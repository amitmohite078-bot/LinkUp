package com.linkup.ranking;

import com.linkup.dto.PostResponse;
import java.util.List;

public interface FeedRankingStrategy {
    List<PostResponse> rank(List<PostResponse> posts, Long currentUserId);
}
