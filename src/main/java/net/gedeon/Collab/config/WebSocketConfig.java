package net.gedeon.Collab.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

/**
 * Configuration WebSocket pour la collaboration temps réel
 *
 * Utilise STOMP (Simple Text Oriented Messaging Protocol) sur WebSocket
 * pour la communication bidirectionnelle entre serveur et clients
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthChannelInterceptor webSocketAuthChannelInterceptor;

    /**
     * Configure le message broker pour les communications pub/sub
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Préfixe pour les messages envoyés du serveur vers les clients
        // Les clients s'abonnent à ces destinations (ex: /topic/document/{documentId})
        registry.enableSimpleBroker("/topic", "/queue");

        // Préfixe pour les messages envoyés des clients vers le serveur
        // Les @MessageMapping dans les controllers utilisent ce préfixe
        registry.setApplicationDestinationPrefixes("/app");

        // Préfixe pour les messages user-specific (curseurs individuels)
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * Configure les endpoints STOMP
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint principal pour la connexion WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // À restreindre en production
                .withSockJS(); // Fallback pour navigateurs sans support WebSocket

        // Endpoint sans SockJS pour clients natifs
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    /**
     * Ajoute l'intercepteur d'authentification JWT
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthChannelInterceptor);
    }
}
