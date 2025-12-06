package ovh.bookexchange.api.services;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ovh.bookexchange.api.domains.entities.EndUser;
import ovh.bookexchange.api.infrastructures.repos.EndUserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class EndUserDetailsService implements UserDetailsService, JwtPasswordResetToken {
    public final static String USER = "USER";
    public final static String ADMIN = "ADMIN";

    private final EndUserRepository endUserRepository;

    public EndUserDetailsService(EndUserRepository endUserRepository) {
        this.endUserRepository = endUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        EndUser endUser = endUserRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("No user found with email: "+ username ));
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(USER));
        if (endUser.isAdmin()) {
            authorities.add(new SimpleGrantedAuthority(ADMIN));
        }
        return new User(endUser.getEmail(), endUser.getPassword(), authorities);
    }

    @Override
    public UserDetails loadUserByUsernameAndToken(String token, boolean isResetPassword) {
        if(isResetPassword){
            String username = token.split("@reset")[0];
            return loadUserByUsername(username);
        }
        return null;
    }
}
