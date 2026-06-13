package ovh.bookexchange.api.controllers;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ovh.bookexchange.api.controllers.representations.messages.MessageRep;
import ovh.bookexchange.api.domains.entities.EndUser;
import ovh.bookexchange.api.domains.entities.GroupChat;
import ovh.bookexchange.api.domains.entities.Message;
import ovh.bookexchange.api.infrastructures.repos.EndUserRepository;
import ovh.bookexchange.api.infrastructures.repos.GroupChatRepository;
import ovh.bookexchange.api.infrastructures.repos.MessageRepository;
import ovh.bookexchange.api.services.NotificationService;

import java.security.Principal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/messages")
public class MessageController {
    private final MessageRepository messageRepo;
    private final GroupChatRepository groupChatRepo;
    private final EndUserRepository userRepo;
    private final ModelMapper mapper;
    private final NotificationService notifService;
    private final SimpMessagingTemplate messagingTemplate; // Ajout pour STOMP

    public MessageController(MessageRepository messageRepo,
                             GroupChatRepository groupChatRepo,
                             EndUserRepository userRepo,
                             NotificationService notifService,
                             ModelMapper mapper,
                             SimpMessagingTemplate messagingTemplate) {
        this.messageRepo = messageRepo;
        this.groupChatRepo = groupChatRepo;
        this.userRepo = userRepo;
        this.mapper = mapper;
        this.notifService = notifService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    @MessageMapping("/chat/{chatId}")
    public void handleChatMessage(
            @DestinationVariable Long chatId,
            @Payload Map<String, String> payload,
            StompHeaderAccessor accessor
    ) {
        // Lire l'email depuis les session attributes
        Map<String, Object> sessionAttrs = accessor.getSessionAttributes();

        if (sessionAttrs == null) {
            throw new IllegalArgumentException("No session attributes found");
        }

        // Clé "email" — cohérent avec ce que l'intercepteur stocke
        String email = (String) sessionAttrs.get("email");

        if (email == null) {
            throw new IllegalArgumentException("User not authenticated");
        }

        EndUser sender = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        GroupChat groupChat = groupChatRepo.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + chatId));

        if (!groupChat.isMember(sender)) {
            throw new IllegalArgumentException("Forbidden");
        }

        Message msg = new Message();
        msg.setContent(payload.get("content"));
        msg.setGroupChat(groupChat);
        msg.setSender(sender);
        msg.setSendTime(Timestamp.valueOf(LocalDateTime.now()));
        msg.setRead(List.of());
        messageRepo.save(msg);

        messagingTemplate.convertAndSend(
                "/topic/chat/" + chatId,
                mapper.map(msg, MessageRep.class)
        );

        sendNotifications(msg);
    }

    private void sendNotifications(Message msg) {
        msg.getGroupChat().getMembers().forEach(membership -> {
            if (membership.getEndUser().getId() == msg.getSender().getId()) return;
            if (!membership.isNotification()) return;
            notifService.sendNotification(msg.getSender().getFirstName(), msg.getContent(), membership.getEndUser().getEmail());
        });
    }

    @GetMapping("/group/{id}")
    public List<MessageRep> getMessages(@PathVariable long id, @ParameterObject Pageable pageable, Principal principal) {
        EndUser user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        GroupChat groupChat = checkGroupChat(id, user);
        return messageRepo.findByGroupChatId(groupChat.getId(), pageable).stream()
                .map(m -> mapper.map(m, MessageRep.class)).toList();
    }

    private GroupChat checkGroupChat(long id, EndUser user) {
        GroupChat groupChat = groupChatRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!groupChat.isMember(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return groupChat;
    }
}