package ovh.bookexchange.api.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import ovh.bookexchange.api.filters.AuthHandshakeInterceptor;
import ovh.bookexchange.api.filters.WebSocketAuthChannelInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AuthHandshakeInterceptor authHandshakeInterceptor;
    private final WebSocketAuthChannelInterceptor channelInterceptor;

    public WebSocketConfig(AuthHandshakeInterceptor authHandshakeInterceptor, WebSocketAuthChannelInterceptor channelInterceptor) {
        this.authHandshakeInterceptor = authHandshakeInterceptor;
        this.channelInterceptor = channelInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Activer un broker simple en mémoire pour les destinations commençant par /topic et /queue
        config.enableSimpleBroker("/topic", "/queue");
        // Définir le préfixe pour les messages envoyés par les clients (ex: /app/send)
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Enregistrer le point de terminaison WebSocket avec l'intercepteur d'authentification
        registry.addEndpoint("/ws")
                // Permettre les connexions depuis le client (ex: http://localhost:5173) et ajouter l'intercepteur d'authentification
                .addInterceptors(authHandshakeInterceptor)
                // Permettre les connexions CORS depuis le client (ex: http://localhost:5173)
                .setAllowedOrigins(
                        "http://localhost:5173",
                        "https://bookexchange-front.vercel.app"
                )
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(channelInterceptor); // propage le Principal dans STOMP
    }
}