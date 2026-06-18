package ovh.bookexchange.api.filters;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNullApi;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import ovh.bookexchange.api.services.EndUserDetailsService;
import ovh.bookexchange.api.services.JwtTokenService;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

/**
 * Ce HandshakeInterceptor est utilisé pour intercepter la handshake WebSocket et tenter d'authentifier l'utilisateur en lisant le cookie "auth_token".
 * Il ne bloque jamais la handshake, même si le cookie est absent ou invalide. L'authentification se fait en lisant le cookie et en stockant l'email + Principal dans les attributes de session.
 * Cela permet à la fois d'avoir une authentification pour les utilisateurs qui ont un cookie valide, tout en permettant aux clients non authentifiés de se connecter et de recevoir des messages publics.
 * L'authentification réelle se fait ensuite dans les handlers de messages STOMP, en lisant les session attributes pour vérifier si l'utilisateur est authentifié ou non.
 */
@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {
    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);
    private final JwtTokenService jwtTokenService;
    private final EndUserDetailsService endUserDetailsService;

    public AuthHandshakeInterceptor(JwtTokenService jwtTokenService, EndUserDetailsService endUserDetailsService) {
        this.jwtTokenService = jwtTokenService;
        this.endUserDetailsService = endUserDetailsService;
    }

    /**
     * Cette méthode est appelée avant la handshake WebSocket. Elle tente d'authentifier l'utilisateur en lisant le cookie "auth_token" et en validant le token JWT.
     * Si le token est valide, elle stocke l'email et un objet Principal (UsernamePasswordAuthenticationToken) dans les session attributes,
     * ce qui permet aux handlers de messages STOMP d'accéder à l'authentification de l'utilisateur.
     * @param request la requête de handshake, qui doit être une instance de ServletServerHttpRequest pour accéder aux cookies
     * @param response la réponse de handshake, qui n'est pas utilisée dans cette méthode mais est nécessaire pour la signature de la méthode
     * @param handler le handler WebSocket qui traite la handshake, qui n'est pas utilisé dans cette méthode mais est nécessaire pour la signature de la méthode
     * @param attributes les attributs de session qui seront disponibles dans les handlers de messages STOMP;
     *                   c'est ici que nous stockons l'email et le Principal si le token est valide. Ces attributs sont
     *                   spécifiques à la handshake WebSocket et ne sont pas partagés avec les requêtes HTTP classiques.
     *                   Ils sont utilisés pour propager l'authentification dans la session WebSocket, mais ne sont pas liés
     *                   à la session HTTP normale. Chaque handshake WebSocket a sa propre session d'attributs, et ces attributs
     *                   sont copiés dans la session WebSocket qui est créée pour cette handshake. Cela signifie que les attributs
     *                   que nous ajoutons ici seront disponibles dans les handlers de messages STOMP via la session WebSocket,
     *                   mais ne seront pas accessibles dans les requêtes HTTP classiques ou dans d'autres handshakes WebSocket.
     *                   En résumé, ces attributs sont un moyen de stocker des informations d'authentification spécifiques à la session
     *                   WebSocket créée par cette handshake, et ils ne sont pas partagés avec les sessions HTTP normales ou d'autres handshakes.
     * session; les handlers de messages STOMP peuvent accéder à ces attributs via la session WebSocket pour vérifier l'authentification de l'utilisateur.
     * @return toujours true pour permettre à la handshake de se poursuivre, même si le cookie est absent ou invalide. L'authentification est gérée en stockant
     *          les informations dans les attributs de session, et les handlers de messages STOMP peuvent vérifier ces attributs pour déterminer si l'utilisateur est authentifié ou non.
     */
    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler handler,
            Map<String, Object> attributes
    ) {
        // On ne bloque jamais la handshake, on laisse passer même sans cookie
        // L'authentification se fait en lisant le cookie et en stockant l'email + Principal dans les attributes
        // Cette ligne permet de s'assurer que la requête est bien une requête HTTP servlet, ce qui est nécessaire pour accéder aux cookies
        if (!(request instanceof ServletServerHttpRequest servletRequest)) return true;

        // 1. Essayer de récupérer le token depuis l'en-tête Authorization (token éphémère pour WebSocket)
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        log.info("WebSocket handshake Authorization header: {}", authHeader);
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            log.info("WebSocket handshake avec token dans Authorization");
        } else {
            // 2. Fallback : lire le cookie HttpOnly (token principal)
            Cookie[] cookies = servletRequest.getServletRequest().getCookies();
            log.info("WebSocket handshake cookies: {}",
                    cookies != null ? Arrays.stream(cookies).map(Cookie::getName).toList() : "null"
            );

            if (cookies != null) {
                token = Arrays.stream(cookies)
                        .filter(c -> "auth_token".equals(c.getName()))
                        .map(Cookie::getValue)
                        .findFirst()
                        .orElse(null);
                if (token != null) {
                    log.info("WebSocket handshake avec cookie auth_token");
                }
            }
        }

        // Si aucun token trouvé, on laisse passer (pas d'auth)
        if (token == null) {
            log.info("WebSocket handshake sans token d'authentification");
            return true;
        }

        // Valider le token (qu'il vienne du header ou du cookie)
        try {
            // Même logique que JwtRequestFilter
            DecodedJWT decodedJWT = jwtTokenService.validateToken(token);

            if (decodedJWT == null
                    || decodedJWT.getSubject() == null
                    || decodedJWT.getExpiresAtAsInstant().isBefore(Instant.now())
            ) {
                log.warn("WebSocket token invalide ou expiré");
                return true; // On ne bloque pas, on laisse passer sans authentifier
            }

            // Si le token est valide, on récupère l'email et on charge les détails de l'utilisateur
            String email = decodedJWT.getSubject();

            UserDetails userDetails = endUserDetailsService.loadUserByUsername(email);

            // Créer un objet d'authentification Spring Security (Principal) à partir des détails de l'utilisateur
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            // Stocker email ET Principal dans les session attributes
            attributes.put("email", email);
            attributes.put("user", auth);

            log.info("WebSocket authentifié : {}", email);

        } catch (Exception e) {
            log.warn("WebSocket auth échouée : {}", e.getMessage());
            // On ne bloque pas, on laisse passer sans authentifier
        }

        return true; // Toujours true — on laisse passer, l'auth est dans les attributs
    }

    /**
     * Cette méthode est appelée après la tentative de handshake, que celle-ci ait réussi ou échoué.
     * Elle peut être utilisée pour effectuer des actions de nettoyage ou de logging après la handshake.
     * Cependant, dans notre cas, nous n'avons pas besoin de faire quoi que ce soit après la handshake, donc cette méthode est vide.
     * @param request la requête de handshake
     * @param response la réponse de handshake
     * @param handler le handler WebSocket qui a traité la handshake
     * @param ex une exception qui s'est produite pendant la handshake, ou null si la handshake a réussi
     */
    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler handler,
            Exception ex
    ) {/* Pas besoin de faire quoi que ce soit après la handshake */}
}