package ovh.bookexchange.api.services;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ovh.bookexchange.api.domains.entities.EndUser;
import ovh.bookexchange.api.infrastructures.repos.EndUserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class EndUserDetailsService implements JwtPasswordResetToken {
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

    /**
     * @param tokenSubject le sujet du token JWT (l'email de l'utilisateur),
     *                     s'il s'agit d'un toke généré pour un reset de mdp, @reset être en début de chaîne.
     */
    @Override
    public UserDetails loadUserByUsernameAndToken(String tokenSubject, boolean isResetPassword) {
        if(isResetPassword){
            String username = tokenSubject.split("@reset")[0];
            return loadUserByUsername(username);
        }
        return null;
    }
}
