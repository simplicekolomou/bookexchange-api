package ovh.bookexchange.api.infrastructures;

import org.springframework.data.repository.CrudRepository;
import ovh.bookexchange.api.domains.entities.EndUser;

import java.util.Optional;

public interface EndUserRepository extends CrudRepository<EndUser, Long> {
    Optional<EndUser> findByEmail(String email);
}
