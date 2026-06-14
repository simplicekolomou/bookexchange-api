package ovh.bookexchange.api.controllers;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ovh.bookexchange.api.controllers.representations.messages.MessageRep;
import ovh.bookexchange.api.domains.entities.Chat;
import ovh.bookexchange.api.domains.entities.EndUser;
import ovh.bookexchange.api.domains.entities.Message;
import ovh.bookexchange.api.infrastructures.repos.EndUserRepository;
import ovh.bookexchange.api.infrastructures.repos.ChatRepository;
import ovh.bookexchange.api.services.MessageService;

import java.security.Principal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/messages")
public class MessageController {
    private final MessageService messageService;
    private final ChatRepository groupChatRepo;
    private final EndUserRepository userRepo;
    private final ModelMapper mapper;
    private final SimpMessagingTemplate messagingTemplate; // Ajout pour STOMP

    public MessageController(
                            MessageService messageService,
                            ChatRepository groupChatRepo,
                            EndUserRepository userRepo,
                            ModelMapper mapper,
                            SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.groupChatRepo = groupChatRepo;
        this.userRepo = userRepo;
        this.mapper = mapper;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    @MessageMapping("/chats/{chatId}")
    public void handleChatMessage(
            @DestinationVariable Long chatId,
            @Payload Map<String, String> payload,
            StompHeaderAccessor accessor
    ) {
        EndUser sender = getEndUserFromStompHeaderAccessor(accessor);
        Chat groupChat = groupChatRepo.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + chatId));

        if (!groupChat.isMember(sender)) {
            throw new IllegalArgumentException("Forbidden");
        }

        Message msg = new Message();
        msg.setContent(payload.get("content"));
        msg.setChat(groupChat);
        msg.setSender(sender);
        msg.setSendTime(Timestamp.valueOf(LocalDateTime.now()));
        msg.setRead(List.of());
        messageService.saveMessage(msg);

        messagingTemplate.convertAndSend(
                "/topic/messages/chats/" + chatId,
                mapper.map(msg, MessageRep.class)
        );

        sendNotifications(msg);
    }

    private EndUser getEndUserFromStompHeaderAccessor(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
        if (sessionAttrs == null) {
            throw new IllegalArgumentException("No session attributes found");
        }

        String email = (String) sessionAttrs.get("email");
        if (email == null) {
            throw new IllegalArgumentException("User not authenticated");
        }

        return userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
    }

    private void sendNotifications(Message msg) {
        msg.getChat().getMembers().forEach(membership -> {
            if (membership.getEndUser().getId() == msg.getSender().getId()) return;
            if (!membership.isNotification()) return;

            String recipientEmail = membership.getEndUser().getEmail();

            // Notification STOMP personnelle — reçue même hors du chat
            messagingTemplate.convertAndSendToUser(
                    recipientEmail,          // ← identifiant de la session STOMP
                    "/queue/notifications",  // ← topic personnel
                    Map.of(
                            "chatId", msg.getChat().getId(),
                            "chatName", msg.getChat().getName(),
                            "senderName", msg.getSender().getFirstName(),
                            "content", msg.getContent(),
                            "sendTime", msg.getSendTime().toString()
                    )
            );
        });
    }

    @GetMapping("/chats/{chatId}")
    public List<MessageRep> getMessages(@PathVariable long chatId, @ParameterObject Pageable pageable, Principal principal) {
        EndUser user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        Chat groupChat = checkGroupChat(chatId, user);
        return messageService.findByGroupChatId(groupChat.getId(), pageable).stream()
                .map(m -> mapper.map(m, MessageRep.class)).toList();
    }

    @PostMapping("/{messageId}/read")
    public ResponseEntity<Void> markRead(@PathVariable long messageId, Principal principal) {
        messageService.markAsRead(messageId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/chats/{chatId}/read")
    public ResponseEntity<Void> markAllRead(@PathVariable long chatId, Principal principal) {
        EndUser user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        messageService.markAllAsRead(chatId, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/chats/{chatId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable long chatId, Principal principal) {
        EndUser user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
       long count = messageService.countUnreadMessages(chatId, user);
        return ResponseEntity.ok(count);
    }

    private Chat checkGroupChat(long groupId, EndUser user) {
        Chat groupChat = groupChatRepo.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!groupChat.isMember(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return groupChat;
    }
}