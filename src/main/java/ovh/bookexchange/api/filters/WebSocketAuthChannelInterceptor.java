package ovh.bookexchange.api.filters;

import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import ovh.bookexchange.api.services.EndUserDetailsService;
import ovh.bookexchange.api.services.JwtTokenService;

import java.time.Instant;
import java.util.Map;

/**
 * Intercepteur de canal pour propager le Principal de Spring Security dans les sessions STOMP.
 * Lors de la connexion, il récupère le Principal stocké lors du handshake et l'associe à la session STOMP.
 * Cela permet d'accéder à l'utilisateur authentifié dans les contrôleurs de messages.
 */
@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {
    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);
    private final JwtTokenService jwtTokenService;
    private final EndUserDetailsService endUserDetailsService;

    public WebSocketAuthChannelInterceptor(JwtTokenService jwtTokenService, EndUserDetailsService endUserDetailsService) {
        this.jwtTokenService = jwtTokenService;
        this.endUserDetailsService = endUserDetailsService;
    }


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

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            // 1. PRIORITÉ : le ticket WebSocket envoyé via connectHeaders côté frontend.
            // getFirstNativeHeader() lit les en-têtes du protocole STOMP lui-même —
            // c'est ICI (et seulement ici) qu'on peut voir ce que connectHeaders a envoyé,
            // jamais dans beforeHandshake qui ne voit que les en-têtes HTTP du handshake.
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7); // on retire le préfixe "Bearer "
                try {
                    DecodedJWT decodedJWT = jwtTokenService.validateToken(token);

                    if (decodedJWT != null
                            && decodedJWT.getSubject() != null
                            && decodedJWT.getExpiresAtAsInstant().isAfter(Instant.now())) {

                        String email = decodedJWT.getSubject();
                        UserDetails userDetails = endUserDetailsService.loadUserByUsername(email);

                        Authentication auth = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                        accessor.setUser(auth);
                        log.info("WebSocket authentifié via ticket STOMP : {}", email);
                        return message; // déjà authentifié, pas besoin du fallback ci-dessous
                    }
                } catch (Exception e) {
                    log.warn("Ticket WebSocket invalide ou expiré : {}", e.getMessage());
                }
            }

            // 2. FALLBACK : comportement que tu avais déjà (Principal posé via le cookie
            // pendant beforeHandshake). On garde ça pour ne rien casser si jamais
            // un client se connecte encore sans ticket.
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                Authentication auth = (Authentication) sessionAttributes.get("user");
                if (auth != null) {
                    accessor.setUser(auth);
                }
            }
        }

        return message;
    }
}
