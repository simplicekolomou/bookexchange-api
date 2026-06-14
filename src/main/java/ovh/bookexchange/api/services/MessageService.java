package ovh.bookexchange.api.services;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ovh.bookexchange.api.domains.entities.EndUser;
import ovh.bookexchange.api.domains.entities.Message;
import ovh.bookexchange.api.infrastructures.repos.EndUserRepository;
import ovh.bookexchange.api.infrastructures.repos.MessageRepository;

import java.util.List;

@Service
@Transactional
public class MessageService {
    private final MessageRepository messageRepository;
    private final EndUserRepository userRepository;

    public MessageService(MessageRepository messageRepository, EndUserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }
    public void markAsRead(Long messageId, String email) {
        Message msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"));
        EndUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!msg.getRead().contains(user) && msg.getSender().getId() != user.getId()) {
            msg.getRead().add(user);
            messageRepository.save(msg); // met à jour la table de liaison
        }
    }

    public void saveMessage(Message message) {
        messageRepository.save(message);
    }

    public Long countUnreadMessages(Long chatId, EndUser user) {
        return messageRepository.findByChatId(chatId, Pageable.unpaged())
                .stream()
                .filter(message -> !message.getRead().contains(user) && message.getSender().getId() != user.getId())
                .count();
    }

    public void markAllAsRead(Long chatId, EndUser user) {
        List<Message> messages = messageRepository.findByChatId(chatId, Pageable.unpaged());
        for (Message msg : messages) {
            if (!msg.getRead().contains(user)) {
                msg.getRead().add(user);
                messageRepository.save(msg); // met à jour la table de liaison
            }
        }
    }

    public List<Message> findByGroupChatId(Long chatId, Pageable pageable){
        return messageRepository.findByChatId(chatId, pageable);
    }
}
