package ovh.bookexchange.api.infrastructures.repos;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import ovh.bookexchange.api.domains.entities.EndUser;

import java.util.Optional;

public interface EndUserRepository extends CrudRepository<EndUser, Long>, JpaSpecificationExecutor<EndUser> {
    Optional<EndUser> findByEmail(String email);
}
