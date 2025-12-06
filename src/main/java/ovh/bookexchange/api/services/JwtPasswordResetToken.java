package ovh.bookexchange.api.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface JwtPasswordResetToken extends UserDetailsService {
    UserDetails loadUserByUsernameAndToken(String email, boolean isResetPassword);
}
