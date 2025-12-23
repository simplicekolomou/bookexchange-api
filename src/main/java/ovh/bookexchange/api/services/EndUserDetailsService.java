package ovh.bookexchange.api.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    /**
     * Cherche des utilisateurs selon les critères donnés.
     * @param firstName Le prénom à chercher (optionnel). Si null ou vide, ce critère est ignoré.
     * @param lastName  Le nom de famille à chercher (optionnel). Si null ou vide, ce critère est ignoré.
     * @param q         Une chaîne de recherche globale (optionnelle). Si fournie, elle est utilisée pour chercher dans les prénoms et noms.
     * @param pageable  Les informations de pagination.
     * @return          Retourne une page d'utilisateurs correspondant aux critères de recherche.
     */
    public Page<EndUser> search(String firstName, String lastName, String q, Pageable pageable) {
        Specification<EndUser> spec = Specification.where(null);

        if (q != null && !q.isBlank()) {
            spec = spec.and(EndUserSpecifications.nameContains(q));
        } else {
            if (firstName != null && !firstName.isBlank()) {
                spec = spec.and(EndUserSpecifications.firstNameContains(firstName));
            }
            if (lastName != null && !lastName.isBlank()) {
                spec = spec.and(EndUserSpecifications.lastNameContains(lastName));
            }
        }

        return endUserRepository.findAll(spec, pageable);
    }
}
