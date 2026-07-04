package ovh.bookexchange.api.services;

import org.springframework.stereotype.Service;
import ovh.bookexchange.api.domains.entities.Chat;
import ovh.bookexchange.api.domains.entities.ChatType;
import ovh.bookexchange.api.domains.entities.Membership;
import ovh.bookexchange.api.infrastructures.repos.ChatRepository;

import java.util.List;

@Service
public class ChatService {
    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public Chat findChatByTypeAndMembersIdIn(ChatType chatType, Long memberId1, Long memberId2) {
        return chatRepository
                .findDirectChatBetweenMembers(chatType, memberId1, memberId2)
                .orElse(null);
    }
}
