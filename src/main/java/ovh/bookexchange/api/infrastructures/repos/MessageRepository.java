package ovh.bookexchange.api.infrastructures.repos;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import ovh.bookexchange.api.domains.entities.Message;

import java.util.List;

public interface MessageRepository extends ListPagingAndSortingRepository<Message, Long>, CrudRepository<Message, Long> {
    List<Message> findByGroupChatId(long id, Pageable pageable);
    @EntityGraph(attributePaths = {"groupChat", "sender"})
    List<Message> findByGroupChatIdIn(List<Long> groupIds);
}
