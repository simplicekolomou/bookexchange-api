package ovh.bookexchange.api.infrastructures.repos;

import org.springframework.data.repository.CrudRepository;
import ovh.bookexchange.api.domains.entities.Chat;

public interface ChatRepository extends CrudRepository<Chat, Long> {
}
