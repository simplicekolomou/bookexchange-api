package ovh.bookexchange.api.filters;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import ovh.bookexchange.api.services.EndUserDetailsService;
import ovh.bookexchange.api.services.JwtTokenService;
import jakarta.servlet.http.Cookie;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;

public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final EndUserDetailsService endUserDetailsService;

    public JwtRequestFilter(JwtTokenService jwtTokenService, EndUserDetailsService endUserDetailsService) {
        this.jwtTokenService = jwtTokenService;
        this.endUserDetailsService = endUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = null;
        // lire depuis le cookie httpOnly
        if (request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(c -> "auth_token".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        // fallback sur le header Authorization (utile pour Swagger, Postman)
        // Ils doivent envoyer le token dans le header Authorization avec le format "Bearer <token>"
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        // si pas de token, continuer sans authentification
        // transmettre la requête au prochain filtre (ex: pour les endpoints publics)
        // le prochain filtre décidera si l'accès est autorisé ou non en fonction de la présence d'une authentification dans le SecurityContext
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // valider le token et extraire les informations
        // si le token est invalide, expiré ou ne contient pas de sujet (email), continuer sans authentification
        // le prochain filtre décidera si l'accès est autorisé ou non en fonction de la présence d'une authentification dans le SecurityContext
        final DecodedJWT decodedJWT = jwtTokenService.validateToken(token);
        if (decodedJWT == null || decodedJWT.getSubject() == null || decodedJWT.getExpiresAtAsInstant().isBefore(Instant.now())) {
            filterChain.doFilter(request, response);
            return;
        }

        // charger les détails de l'utilisateur à partir du sujet (email) du token
        UserDetails userDetails;
        try {
            userDetails = endUserDetailsService.loadUserByUsername(decodedJWT.getSubject());
        } catch (UsernameNotFoundException e) {
            filterChain.doFilter(request, response);
            return;
        }

        // créer une authentification Spring Security et la stocker dans le contexte de sécurité
        // le token est déjà validé, donc on peut faire confiance au sujet (email) et aux rôles extraits du token
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // continuer la chaîne de filtres avec l'utilisateur authentifié dans le contexte de sécurité
        filterChain.doFilter(request, response);
    }
}
