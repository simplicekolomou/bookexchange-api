package ovh.bookexchange.api.filters;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Intercepteur de canal pour propager le Principal de Spring Security dans les sessions STOMP.
 * Lors de la connexion, il récupère le Principal stocké lors du handshake et l'associe à la session STOMP.
 * Cela permet d'accéder à l'utilisateur authentifié dans les contrôleurs de messages.
 */
@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    /**
     * Intercepte les messages entrants sur le canal STOMP. Lors d'une commande CONNECT, il récupère le Principal
     * stocké dans les session attributes (par l'AuthHandshakeInterceptor) et l'associe à la session STOMP via accessor.setUser(auth).
     * Cela permet aux contrôleurs de messages d'accéder à l'utilisateur authentifié via accessor.getUser() ou @AuthenticationPrincipal.
     * @param message le message STOMP entrant
     * @param channel le canal sur lequel le message est envoyé
     * @return le message modifié avec le Principal associé à la session STOMP, ou le message original si aucun changement n'est nécessaire
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        // Seule la commande CONNECT nécessite de propager le Principal, les autres commandes (SEND, SUBSCRIBE, etc.)
        // utilisent déjà le Principal associé à la session
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // récupère le Principal stocké lors du handshake
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                Authentication auth = (Authentication) sessionAttributes.get("user");
                if (auth != null) {
                    // injecte le Principal dans la session STOMP
                    accessor.setUser(auth);
                }
            }
        }

        return message;
    }
}
