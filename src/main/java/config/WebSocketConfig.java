package com.typearena.config;

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
        // İstemcilerin bağlanacağı ana kapı. Güvenlik kilidini kaldırıp herkese izin veriyoruz.
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Sunucudan oyunculara gidecek mesaj kanalı
        registry.enableSimpleBroker("/topic");
        // Oyunculardan sunucuya gelecek mesaj kanalı
        registry.setApplicationDestinationPrefixes("/app");
    }
}