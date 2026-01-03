package ovh.bookexchange.api.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ovh.bookexchange.api.controllers.representations.messages.GroupChatRep;
import ovh.bookexchange.api.domains.entities.EndUser;
import ovh.bookexchange.api.domains.entities.GroupChat;
import ovh.bookexchange.api.domains.entities.Membership;
import ovh.bookexchange.api.infrastructures.repos.EndUserRepository;
import ovh.bookexchange.api.infrastructures.repos.GroupChatRepository;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/groups")
public class GroupController {

    private final GroupChatRepository groupRepo;
    private final EndUserRepository userRepo;
    private final ModelMapper mapper;

    public GroupController(GroupChatRepository groupRepo, EndUserRepository userRepo, ModelMapper mapper) {
        this.groupRepo = groupRepo;
        this.userRepo = userRepo;
        this.mapper = mapper;
    }

    @PostMapping
    public void createGroup(@RequestBody @Valid GroupChatRep groupChatRep) {
        GroupChat group = mapper.map(groupChatRep, GroupChat.class);
        setMembersAndName(groupChatRep, group);
        group.setMessages(List.of());
        groupRepo.save(group);
    }

    @GetMapping("/user/me")
    public List<GroupChatRep> getMyGroups(Principal principal) {
        EndUser user = findUserOr500(principal);
        List<Membership> memberships = user.getMemberships();
        return memberships.stream().map(m -> mapper.map(m.getGroupChat(), GroupChatRep.class)).toList();
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
        group.setMembers(
                groupChatRep.getMembers().stream().map(mr -> {
                    Membership ms = new Membership();
                    ms.setGroupChat(group);
                    ms.setEndUser(userRepo.findById(mr.getEndUserId()).orElseThrow(()
                            -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));
                    ms.setNotification(mr.isNotification());
                    return ms;
                }).toList()
        );
        if (groupChatRep.getName() == null || groupChatRep.getName().isBlank()) {
            group.setName(
                group.getMembers().stream()
                        .map(membership -> membership.getEndUser().getFirstName())
                        .collect(java.util.stream.Collectors.joining(", "))
            );
            return;
        }
        group.setName(groupChatRep.getName());

    }

    private GroupChat findGroupOr404(long id) {
        return groupRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
    }

    private EndUser findUserOr500(Principal principal) {
        return userRepo.findByEmail(principal.getName()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Logged in user not found"));
    }
}
