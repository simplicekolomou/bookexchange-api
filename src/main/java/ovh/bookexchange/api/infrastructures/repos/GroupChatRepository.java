package ovh.bookexchange.api.infrastructures.repos;

import org.springframework.data.repository.CrudRepository;
import ovh.bookexchange.api.domains.entities.GroupChat;

public interface GroupChatRepository extends CrudRepository<GroupChat, Long> {
}
