package ovh.bookexchange.api.controllers.representations.messages;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class GroupChatRep {
    private long id;

    @Valid
    @Size(min = 2, max = 20)
    private List<MembershipRep> members;
}
