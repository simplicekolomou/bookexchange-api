package ovh.bookexchange.api.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ovh.bookexchange.api.controllers.representations.messages.GroupChatRep;
import ovh.bookexchange.api.controllers.representations.messages.MessageRep;
import ovh.bookexchange.api.domains.entities.EndUser;
import ovh.bookexchange.api.domains.entities.GroupChat;
import ovh.bookexchange.api.domains.entities.Membership;
import ovh.bookexchange.api.domains.entities.Message;
import ovh.bookexchange.api.infrastructures.repos.EndUserRepository;
import ovh.bookexchange.api.infrastructures.repos.GroupChatRepository;
import ovh.bookexchange.api.infrastructures.repos.MessageRepository;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/groups")
public class GroupController {

    private final GroupChatRepository groupRepo;
    private final EndUserRepository userRepo;
    private final ModelMapper mapper;
    private final MessageRepository messageRepository;

    public GroupController(GroupChatRepository groupRepo, EndUserRepository userRepo, ModelMapper mapper, MessageRepository messageRepository) {
        this.groupRepo = groupRepo;
        this.userRepo = userRepo;
        this.mapper = mapper;
        this.messageRepository = messageRepository;
    }

    @PostMapping
    public void createGroup(@RequestBody @Valid GroupChatRep groupChatRep) {
        System.out.println("Creating group with type: " + groupChatRep);
        GroupChat group = mapper.map(groupChatRep, GroupChat.class);
        setMembersAndName(groupChatRep, group);
        group.setMessages(List.of());
        groupRepo.save(group);
    }

    @GetMapping("/user/me")
    public List<GroupChatRep> getMyGroups(Principal principal) {
        EndUser user = findUserOr500(principal);
        List<Membership> memberships = user.getMemberships();

        if (memberships.isEmpty()) return List.of();

        List<Long> groupIds = memberships.stream()
                .map(m -> m.getGroupChat().getId())
                .toList();

        Map<Long, Message> lastMessageByGroupId = messageRepository
                .findByGroupChatIdIn(groupIds)
                .stream()
                .collect(Collectors.toMap(
                        m -> m.getGroupChat().getId(),
                        m -> m,
                        (m1, m2) -> m1.getSendTime().compareTo(m2.getSendTime()) >= 0 ? m1 : m2
                ));

        return memberships.stream()
                .map(Membership::getGroupChat)
                .sorted((g1, g2) -> {
                    Message m1 = lastMessageByGroupId.get(g1.getId());
                    Message m2 = lastMessageByGroupId.get(g2.getId());
                    if (m1 == null && m2 == null) return 0;
                    if (m1 == null) return 1;
                    if (m2 == null) return -1;
                    return m2.getSendTime().compareTo(m1.getSendTime());
                })
                .map(g -> {
                    GroupChatRep rep = mapper.map(g, GroupChatRep.class);
                    Message lastMsg = lastMessageByGroupId.get(g.getId());
                    if (lastMsg != null) {
                        // ✅ conversion avec ModelMapper
                        MessageRep msgRep = mapper.map(lastMsg, MessageRep.class);
                        rep.setLastMessage(msgRep);
                    } else {
                        rep.setLastMessage(null);
                    }
                    return rep;
                })
                .toList();
    }

    @DeleteMapping("/{id}")
    public void deleteGroup(@PathVariable long id, Principal principal) {
        EndUser user = findUserOr500(principal);
        GroupChat group = findGroupOr404(id);
        if (!group.isMember(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        groupRepo.delete(group);
    }

    @PutMapping("/{id}")
    public void editGroup(@PathVariable long id, @RequestBody @Valid GroupChatRep groupChatRep, Principal principal) {
        EndUser user = findUserOr500(principal);
        GroupChat group = findGroupOr404(id);
        if (!group.isMember(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        setMembersAndName(groupChatRep, group);
        groupRepo.save(group);
    }

    private void setMembersAndName(GroupChatRep groupChatRep, GroupChat group) {
        //TODO this should be in domain or model mapper config.
        List<Membership> members = group.getMembers();
        members.clear();
        members.addAll(
                groupChatRep.getMembers().stream().map(mr -> {
                    Membership ms = new Membership();
                    ms.setGroupChat(group);
                    ms.setEndUser(userRepo.findById(mr.getEndUserId()).orElseThrow(()
                            -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));
                    ms.setNotification(mr.isNotification());
                    return ms;
                }).collect(Collectors.toList())
        );
        group.setName(groupChatRep.getName());
    }

    private GroupChat findGroupOr404(long id) {
        return groupRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
    }

    private EndUser findUserOr500(Principal principal) {
        return userRepo.findByEmail(principal.getName()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Logged in user not found"));
    }

    @GetMapping("/oneToOne/{memberId}")
    public GroupChatRep getGroupByMembers(@PathVariable Long memberId, Principal principal) {
        EndUser currentUser = findUserOr500(principal);
        EndUser targetUser = userRepo.findById(memberId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Target user not found"));
        List<GroupChat> groups = findUserOr500(principal).getMemberships().stream()
                .map(Membership::getGroupChat)
                .filter(groupChat -> groupChat.isMember(targetUser) && groupChat.getMembers().size() == 2)
                .toList();

        if (groups.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One-to-one group not found");
        }
        return mapper.map(groups.get(0), GroupChatRep.class);
    }
}
