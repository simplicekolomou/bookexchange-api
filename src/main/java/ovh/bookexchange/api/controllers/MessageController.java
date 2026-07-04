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
import ovh.bookexchange.api.domains.entities.Membership;
import ovh.bookexchange.api.domains.entities.Message;
import ovh.bookexchange.api.infrastructures.repos.EndUserRepository;
import ovh.bookexchange.api.infrastructures.repos.ChatRepository;
import ovh.bookexchange.api.services.FirebaseService;
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
    private final FirebaseService firebaseService; // Ajout pour FCM

    public MessageController(
                            MessageService messageService,
                            ChatRepository groupChatRepo,
                            EndUserRepository userRepo,
                            ModelMapper mapper,
                            SimpMessagingTemplate messagingTemplate,
                            FirebaseService firebaseService) {
        this.messageService = messageService;
        this.groupChatRepo = groupChatRepo;
        this.userRepo = userRepo;
        this.mapper = mapper;
        this.messagingTemplate = messagingTemplate;
        this.firebaseService = firebaseService;
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
        // accessor.getUser() retourne le Principal injecté par setUser()
        // dans ton StompAuthChannelInterceptor au moment du CONNECT STOMP.
        // C'est la nouvelle source d'authentification, qui remplace les session attributes.
        Principal principal = accessor.getUser();

        if (principal == null) {
            throw new IllegalArgumentException("User not authenticated");
        }

        // principal.getName() retourne le username de l'objet UserDetails
        // passé au constructeur de UsernamePasswordAuthenticationToken.
        // Dans ton cas c'est l'email, puisque tu utilises loadUserByUsername(email).
        String email = principal.getName();

        return userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
    }

    private void sendNotifications(Message message) {
        // Pour chaque membre du chat différent de l'expéditeur, tenter d'envoyer une notification push
        for (Membership member : message.getChat().getMembers()) {
            EndUser memberUser = member.getEndUser();
            if (memberUser.getId() != message.getSender().getId() && memberUser.getFcmToken() != null) {
                try {
                    firebaseService.sendNotificationToToken(
                            memberUser.getFcmToken(),
                            "Nouveau message de " + message.getSender().getFirstName(),
                            message.getContent().length() > 100 ?
                                    message.getContent().substring(0, 100) + "..." :
                                    message.getContent()
                    );
                } catch (Exception e) {
                    System.err.println("Impossible d'envoyer la notification push à l'utilisateur " + memberUser.getId() + " : " + e.getMessage());
                    // Option : supprimer le token s'il est invalide
                    if (e.getMessage().contains("404") || e.getMessage().contains("Invalid registration token")) {
                        memberUser.setFcmToken(null);
                        firebaseService.saveFcmToken(memberUser.getEmail(), memberUser.getFcmToken());
                    }
                }
            }
        }
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
    public ResponseEntity<Long> markRead(@PathVariable long messageId, Principal principal) {
        messageService.markAsRead(messageId, principal.getName());
        return ResponseEntity.ok(messageId);
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