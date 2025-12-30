package ovh.bookexchange.api.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ovh.bookexchange.api.controllers.representations.messages.MessageRep;
import ovh.bookexchange.api.domains.entities.EndUser;
import ovh.bookexchange.api.domains.entities.GroupChat;
import ovh.bookexchange.api.domains.entities.Message;
import ovh.bookexchange.api.infrastructures.repos.EndUserRepository;
import ovh.bookexchange.api.infrastructures.repos.GroupChatRepository;
import ovh.bookexchange.api.infrastructures.repos.MessageRepository;

import java.security.Principal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/messages")
public class MessageController {
    private final MessageRepository messageRepo;
    private final GroupChatRepository groupChatRepo;
    private final EndUserRepository userRepo;
    private final ModelMapper mapper;

    public MessageController(MessageRepository messageRepo, GroupChatRepository groupChatRepo, EndUserRepository userRepo, ModelMapper mapper) {
        this.messageRepo = messageRepo;
        this.groupChatRepo = groupChatRepo;
        this.userRepo = userRepo;
        this.mapper = mapper;
    }

    @PostMapping("/group/{id}")
    public void sendMessage(@PathVariable long id, @RequestBody @Valid MessageRep message, Principal principal) {
        EndUser sender = userRepo.findByEmail(principal.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        GroupChat groupChat = checkGroupChat(id, sender);
        Message msg = new Message();
        msg.setContent(message.getContent());
        msg.setGroupChat(groupChat);
        msg.setSender(sender);
        msg.setSendTime(Timestamp.valueOf(LocalDateTime.now()));
        msg.setRead(List.of());
        messageRepo.save(msg);
    }

    @GetMapping("/group/{id}")
    public List<MessageRep> getMessages(@PathVariable long id, @ParameterObject Pageable pageable, Principal principal){
        EndUser user = userRepo.findByEmail(principal.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        GroupChat groupChat = checkGroupChat(id, user);
        return messageRepo.findByGroupChatId(groupChat.getId(), pageable).stream().map(m -> mapper.map(m, MessageRep.class)).toList();
    }

    private GroupChat checkGroupChat(long id, EndUser user) {
        GroupChat groupChat = groupChatRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!groupChat.isMember(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return groupChat;
    }
}
