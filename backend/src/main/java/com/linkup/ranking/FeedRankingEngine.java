package com.linkup.ranking;

import com.linkup.dto.PostResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FeedRankingEngine {

    private final Map<String, FeedRankingStrategy> strategies;
    
    private String defaultStrategy = "personalized";

    @Autowired
    public FeedRankingEngine(
            @Qualifier("chronological") FeedRankingStrategy chronological,
            @Qualifier("engagement") FeedRankingStrategy engagement,
            @Qualifier("relationship") FeedRankingStrategy relationship,
            @Qualifier("personalized") FeedRankingStrategy personalized) {
        this.strategies = Map.of(
                "chronological", chronological,
                "engagement", engagement,
                "relationship", relationship,
                "personalized", personalized
        );
    }

    public List<PostResponse> rank(List<PostResponse> posts, Long currentUserId, String strategyName) {
        String strategyKey = (strategyName == null || !strategies.containsKey(strategyName.toLowerCase())) 
                ? defaultStrategy 
                : strategyName.toLowerCase();
        
        return strategies.get(strategyKey).rank(posts, currentUserId);
    }

    public void setDefaultStrategy(String strategy) {
        if (strategies.containsKey(strategy.toLowerCase())) {
            this.defaultStrategy = strategy.toLowerCase();
        }
    }

    public String getDefaultStrategy() {
        return defaultStrategy;
    }
}
