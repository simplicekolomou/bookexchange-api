package ovh.bookexchange.api.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ovh.bookexchange.api.controllers.representations.messages.ChatRep;
import ovh.bookexchange.api.controllers.representations.messages.MessageRep;
import ovh.bookexchange.api.domains.entities.Chat;
import ovh.bookexchange.api.domains.entities.EndUser;
import ovh.bookexchange.api.domains.entities.Membership;
import ovh.bookexchange.api.domains.entities.Message;
import ovh.bookexchange.api.infrastructures.repos.EndUserRepository;
import ovh.bookexchange.api.infrastructures.repos.ChatRepository;
import ovh.bookexchange.api.infrastructures.repos.MessageRepository;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/chats")
public class ChatController {

    private final ChatRepository chatRepo;
    private final EndUserRepository userRepo;
    private final ModelMapper mapper;
    private final MessageRepository messageRepository;

    public ChatController(ChatRepository chatRepo, EndUserRepository userRepo, ModelMapper mapper, MessageRepository messageRepository) {
        this.chatRepo = chatRepo;
        this.userRepo = userRepo;
        this.mapper = mapper;
        this.messageRepository = messageRepository;
    }

    @PostMapping
    public ChatRep createChat(@RequestBody @Valid ChatRep chatRep) {
        Chat chat = mapper.map(chatRep, Chat.class);
        setMembersAndName(chatRep, chat);
        chat.setMessages(List.of());
        chatRepo.save(chat);
        return mapper.map(chat, ChatRep.class);
    }

    @GetMapping("/user/me")
    public List<ChatRep> getMyChats(Principal principal) {
        EndUser user = findUserOr500(principal);
        List<Membership> memberships = user.getMemberships();

        if (memberships.isEmpty()) return List.of();

        List<Long> chatIds = memberships.stream()
                .map(m -> m.getChat().getId())
                .toList();

        Map<Long, Message> lastMessageByChatId = messageRepository
                .findByChatIdIn(chatIds)
                .stream()
                .collect(Collectors.toMap(
                        m -> m.getChat().getId(),
                        m -> m,
                        (m1, m2) -> m1.getSendTime().compareTo(m2.getSendTime()) >= 0 ? m1 : m2
                ));

        return memberships.stream()
                .map(Membership::getChat)
                .sorted((g1, g2) -> {
                    Message m1 = lastMessageByChatId.get(g1.getId());
                    Message m2 = lastMessageByChatId.get(g2.getId());
                    if (m1 == null && m2 == null) return 0;
                    if (m1 == null) return 1;
                    if (m2 == null) return -1;
                    return m2.getSendTime().compareTo(m1.getSendTime());
                })
                .map(g -> {
                    ChatRep rep = mapper.map(g, ChatRep.class);
                    Message lastMsg = lastMessageByChatId.get(g.getId());
                    if (lastMsg != null) {
                        // conversion avec ModelMapper
                        MessageRep msgRep = mapper.map(lastMsg, MessageRep.class);
                        rep.setLastMessage(msgRep);
                    } else {
                        rep.setLastMessage(null);
                    }
                    return rep;
                })
                .toList();
    }

    @DeleteMapping("/{chatId}")
    public void deleteChat(@PathVariable long chatId, Principal principal) {
        EndUser user = findUserOr500(principal);
        Chat chat = findChatOr404(chatId);
        if (!chat.isMember(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        chatRepo.delete(chat);
    }

    @PutMapping("/{id}")
    public void editChat(@PathVariable long id, @RequestBody @Valid ChatRep chatRep, Principal principal) {
        EndUser user = findUserOr500(principal);
        Chat chat = findChatOr404(id);
        if (!chat.isMember(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        setMembersAndName(chatRep, chat);
        chatRepo.save(chat);
    }

    private void setMembersAndName(ChatRep chatRep, Chat chat) {
        //TODO this should be in domain or model mapper config.
        List<Membership> members = chat.getMembers();
        members.clear();
        members.addAll(
                chatRep.getMembers().stream().map(mr -> {
                    Membership ms = new Membership();
                    ms.setChat(chat);
                    ms.setEndUser(userRepo.findById(mr.getEndUserId()).orElseThrow(()
                            -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));
                    ms.setNotification(mr.isNotification());
                    return ms;
                }).collect(Collectors.toList())
        );
        chat.setName(chatRep.getName());
    }

    private Chat findChatOr404(long id) {
        return chatRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
    }

    private EndUser findUserOr500(Principal principal) {
        return userRepo.findByEmail(principal.getName()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Logged in user not found"));
    }

    /*@GetMapping("/one-to-one/{memberId}")
    public ChatRep getGroupByMembers(@PathVariable Long memberId, Principal principal) {
        EndUser targetUser = userRepo.findById(memberId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Target user not found"));
        List<Chat> groups = findUserOr500(principal).getMemberships().stream()
                .map(Membership::getGroupChat)
                .filter(groupChat -> groupChat.isMember(targetUser) && groupChat.getMembers().size() == 2)
                .toList();

        if (groups.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One-to-one group not found");
        }
        return mapper.map(groups.get(0), ChatRep.class);
    }*/
}
