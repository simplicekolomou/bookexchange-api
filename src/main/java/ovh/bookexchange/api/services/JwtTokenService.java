package ovh.bookexchange.api.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;
import ovh.bookexchange.api.controllers.representations.TokenType;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Service
public class JwtTokenService {
    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    private final Duration duration;
    private final Duration wsTokenDuration;
    private final Duration resetDuration;

    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtTokenService(Environment environment) {
        this.algorithm = Algorithm.HMAC512(Objects.requireNonNull(environment.getProperty("JWT_SECRET"), "JWT_SECRET must not be null"));
        this.verifier = JWT.require(this.algorithm).build();
        this.wsTokenDuration = Duration.ofMinutes(environment.getProperty("JWT_WS_TOKEN_DURATION", Long.class, 60L));
        this.duration = Duration.ofMinutes(Long.parseLong(Objects.requireNonNull(environment.getProperty("JWT_DURATION"), "JWT_DURATION must not be null")));
        this.resetDuration = Duration.ofMinutes(environment.getProperty("JWT_RESET_DURATION", Long.class, 15L));
    }

    public String generateToken(UserDetails userDetails, TokenType tokenType) {
        final Instant now = Instant.now();
        return switch (tokenType){
            case WS_TOKEN -> JWT.create()
                    .withSubject(userDetails.getUsername())
                    .withIssuedAt(now)
                    .withExpiresAt(now.plus(wsTokenDuration))
                    .sign(algorithm);
            case AUTH_TOKEN -> JWT.create()
                    .withSubject(userDetails.getUsername())
                    .withIssuedAt(now)
                    .withExpiresAt(now.plus(duration))
                    .sign(algorithm);
            case RESET_PASSWORD_TOKEN -> JWT.create()
                    .withSubject(userDetails.getUsername() + "@reset")
                    .withIssuedAt(now)
                    .withExpiresAt(now.plus(resetDuration))
                    .sign(algorithm);
        };
    }

    public DecodedJWT validateToken(String token) {
        try {
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            log.warn("Invalid JWT token : {}", e.getMessage());
            return null;
        }
    }
}
