package ovh.bookexchange.api.infrastructures.repos;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ovh.bookexchange.api.domains.entities.Chat;
import ovh.bookexchange.api.domains.entities.ChatType;

import java.util.Optional;

public interface ChatRepository extends CrudRepository<Chat, Long> {
    @Query("""
        SELECT c FROM Chat c
        JOIN c.members m1
        JOIN c.members m2
        WHERE c.chatType = :chatType
          AND m1.endUser.id = :memberId1
          AND m2.endUser.id = :memberId2
          AND (SELECT COUNT(m) FROM c.members m) = 2
        """)
    Optional<Chat> findDirectChatBetweenMembers(
            @Param("chatType") ChatType chatType,
            @Param("memberId1") Long memberId1,
            @Param("memberId2") Long memberId2
    );
}
