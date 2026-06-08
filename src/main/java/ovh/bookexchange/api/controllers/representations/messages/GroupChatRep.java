package ovh.bookexchange.api.controllers.representations.messages;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ovh.bookexchange.api.domains.entities.GroupType;

import java.util.List;

@Setter
@Getter
@ToString
public class GroupChatRep {
    private long id;

    private String name;

    private GroupType groupType;
    private MessageRep lastMessage;

    @Valid
    @Size(min = 2, max = 20)
    private List<MembershipRep> members;
}
