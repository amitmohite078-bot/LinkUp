package com.linkup.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Core endpoint for websocket connection
        registry.addEndpoint("/ws-linkup")
                .setAllowedOriginPatterns("*");
        
        // SockJS fallback option
        registry.addEndpoint("/ws-linkup")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enact subscription channels
        config.enableSimpleBroker("/topic");
        
        // Enact application dispatch endpoints
        config.setApplicationDestinationPrefixes("/app");
    }
}
