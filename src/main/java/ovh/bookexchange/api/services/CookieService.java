package ovh.bookexchange.api.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CookieService {

    @Value("${COOKIE_SECURE}")
    private boolean secure;

    @Value("${COOKIE_SAME_SITE}")
    private String sameSite;

    @Value("${COOKIE_MAX_AGE_DAYS}")
    private long maxAgeDays;

    // Crée le cookie auth
    public ResponseCookie createAuthCookie(String token) {
        return ResponseCookie.from("auth_token", token)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .maxAge(Duration.ofDays(maxAgeDays))
                .path("/")
                .build(); 
    }

    // Cookie vide pour le logout
    public ResponseCookie clearAuthCookie() {
        return ResponseCookie.from("auth_token", "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .maxAge(Duration.ZERO)  // expire immédiatement
                .path("/")
                .build();
    }
}
